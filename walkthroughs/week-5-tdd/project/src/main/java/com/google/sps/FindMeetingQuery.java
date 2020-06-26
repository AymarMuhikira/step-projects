// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.*;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<Event> requiredEvents = new ArrayList<>();
    List<Event> optionalEvents = new ArrayList<>();  

    for(Event event: events) {
      if (hasRequiredAttendees(event, request)) {
        requiredEvents.add(event);  
      }
      //Only check for optional attendees if the event has no required one
      else if (hasOptionalAttendees(event, request)) {
        optionalEvents.add(event);    
      }
    }

    List<TimeRange> requiredTimes = getRequiredTime(requiredEvents, request.getDuration());
    List<TimeRange> optionalTimes = getOptionalTime(optionalEvents, request.getDuration(), requiredTimes);

    if (optionalTimes.size() > 0) {
      return optionalTimes;  
    }
    return requiredTimes;
  }

  private List<TimeRange> getTime(Collection<Event> events, long requestDuration, List<TimeRange> initial) {
    List<TimeRange> meetingTimes = initial;

    for (Event event: events) {
      List<TimeRange> temp = new ArrayList<>();
      for (TimeRange time: meetingTimes) {
        //time:     |--------------|
        //event:         |----|
        //division: |----|    |----|
        if (time.contains(event.getWhen())) {
          TimeRange firstDivision = TimeRange.fromStartEnd(time.start(), event.getWhen().start(), false);
          TimeRange secondDivision = TimeRange.fromStartEnd(event.getWhen().end(), time.end(), false);
          //Use END_OF_DAY + 1 because the end() returns the open interval end, so for an event at the end of the day, this value is 1440, not 1439.
          if (firstDivision.duration() >= requestDuration && firstDivision.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(firstDivision);  
          }
          if (secondDivision.duration() >= requestDuration && secondDivision.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(secondDivision);  
          }
        }
        //time:     |--------------|
        //event:         |------------|
        //division: |----|
        else if (time.contains(event.getWhen().start())) {
          TimeRange division = TimeRange.fromStartEnd(time.start(), event.getWhen().start(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(division);
          }
        }
        //time:     |--------------|
        //event:  |-----------|
        //division:           |----|
        else if (time.contains(event.getWhen().end())) {
          TimeRange division = TimeRange.fromStartEnd(event.getWhen().end(), time.end(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(division);  
          }
        }
        //time:     |------|
        //event:             |----|
        //division: |------|
        //make sure to avoid/do not include case where the time is entirely contained in the event
        //avoid-time:     |----|
        //avoid-event: |-----------|
        else if (!event.getWhen().contains(time)){
          temp.add(time);  
        } 
      }
      meetingTimes = temp;
    }

    return meetingTimes;  
  }

  private List<TimeRange> getRequiredTime(Collection<Event> events, long requestDuration) {
    List<TimeRange> meetingTimes = new ArrayList<>();  
    TimeRange initial = TimeRange.WHOLE_DAY;

    if(initial.duration() >= requestDuration) {
      meetingTimes.add(initial);  
    }

    return getTime(events, requestDuration, meetingTimes);
  }

  private List<TimeRange> getOptionalTime(Collection<Event> events, long requestDuration, List<TimeRange> requiredTimes) {
    return getTime(events, requestDuration, requiredTimes);
  }

  private boolean hasRequiredAttendees(Event event, MeetingRequest request) {
    Collection<String> eventAttendees = event.getAttendees();
    Collection<String> meetingAttendees = request.getAttendees();  
    boolean hasAttendees = !Collections.disjoint(eventAttendees, meetingAttendees);
    return hasAttendees;
  }

  private boolean hasOptionalAttendees(Event event, MeetingRequest request) {
    Collection<String> eventAttendees = event.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    boolean hasAttendees = !Collections.disjoint(eventAttendees, optionalAttendees);
    return hasAttendees;
  }

  private List<TimeRange> getTimeAlternative(Collection<Event> events, long requestDuration, List<TimeRange> initial) {
    //Use sort and binary search instead of looping through potential meeting times
    //This implementation does not give the expected results yet.
    List<TimeRange> meetingTimes = initial;

    for (Event event: events) {
      Collections.sort(meetingTimes, TimeRange.ORDER_BY_START);
      int upperIndex = Collections.binarySearch(meetingTimes, event.getWhen(), TimeRange.ORDER_BY_START);
      if(upperIndex < meetingTimes.size()) {
        TimeRange upperTime = meetingTimes.get(upperIndex);
        //time:     |--------------|
        //event:         |----|
        //division: |----|    |----|
        if(upperTime.contains(event.getWhen())) {
          meetingTimes.remove(upperIndex);
          TimeRange firstDivision = TimeRange.fromStartEnd(upperTime.start(), event.getWhen().start(), false);
          TimeRange secondDivision = TimeRange.fromStartEnd(event.getWhen().end(), upperTime.end(), false);
          //Use END_OF_DAY + 1 because the end() returns the open interval end, so for an event at the end of the day, this value is 1440, not 1439.
          if (firstDivision.duration() >= requestDuration && firstDivision.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(firstDivision);  
          }
          if (secondDivision.duration() >= requestDuration && secondDivision.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(secondDivision);  
          }
        }
        //time:     |--------------|
        //event:         |------------|
        //division: |----|
        else if (upperTime.contains(event.getWhen().start())) {
          meetingTimes.remove(upperIndex);
          TimeRange division = TimeRange.fromStartEnd(upperTime.start(), event.getWhen().start(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(division);
          }
        }
        //time:     |--------------|
        //event:  |-----------|
        //division:           |----|
        else if (upperTime.contains(event.getWhen().end())) {
          meetingTimes.remove(upperIndex);
          TimeRange division = TimeRange.fromStartEnd(event.getWhen().end(), upperTime.end(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(division);  
          }
        }
        //make sure to avoid/do not include case where the time is entirely contained in the event
        //avoid-time:     |----|
        //avoid-event: |-----------|
        else if (event.getWhen().contains(upperTime)){
          meetingTimes.remove(upperIndex);  
        } 
      }

      int lowerIndex = upperIndex - 1;

      if(lowerIndex >= 0) {
        TimeRange lowerTime = meetingTimes.get(lowerIndex);
        //time:     |--------------|
        //event:         |----|
        //division: |----|    |----|
        if(lowerTime.contains(event.getWhen())) {
          meetingTimes.remove(lowerIndex);
          TimeRange firstDivision = TimeRange.fromStartEnd(lowerTime.start(), event.getWhen().start(), false);
          TimeRange secondDivision = TimeRange.fromStartEnd(event.getWhen().end(), lowerTime.end(), false);
          //Use END_OF_DAY + 1 because the end() returns the open interval end, so for an event at the end of the day, this value is 1440, not 1439.
          if (firstDivision.duration() >= requestDuration && firstDivision.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(firstDivision);  
          }
          if (secondDivision.duration() >= requestDuration && secondDivision.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(secondDivision);  
          }
        }
        //time:     |--------------|
        //event:         |------------|
        //division: |----|
        else if (lowerTime.contains(event.getWhen().start())) {
          meetingTimes.remove(lowerIndex);
          TimeRange division = TimeRange.fromStartEnd(lowerTime.start(), event.getWhen().start(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(division);
          }
        }
        //time:     |--------------|
        //event:  |-----------|
        //division:           |----|
        else if (lowerTime.contains(event.getWhen().end())) {
          meetingTimes.remove(lowerIndex);
          TimeRange division = TimeRange.fromStartEnd(event.getWhen().end(), lowerTime.end(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            meetingTimes.add(division);  
          }
        }
        //make sure to avoid/do not include case where the time is entirely contained in the event
        //avoid-time:     |----|
        //avoid-event: |-----------|
        else if (event.getWhen().contains(lowerTime)){
          meetingTimes.remove(lowerIndex);  
        } 
      }
    }

    return meetingTimes;  
  }
}

