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
      int type = eventType(event, request);
      if (type == 1) {
        requiredEvents.add(event);  
      }
      else if (type == 2) {
        optionalEvents.add(event);    
      }
    }

    Collection<TimeRange> requiredTimes = getRequiredTime(requiredEvents, request.getDuration());
    Collection<TimeRange> optionalTimes = getOptionalTime(optionalEvents, request.getDuration(), requiredTimes);

    if(requiredEvents.size() == 0 || optionalTimes.size() > 0) {
      return optionalTimes;  
    }
    return requiredTimes;
  }

  private Collection<TimeRange> getTime(Collection<Event> events, long requestDuration, Collection<TimeRange> initial) {
    Collection<TimeRange> meetingTimes = initial;

    for (Event event: events) {
      List<TimeRange> temp = new ArrayList<>();
      for (TimeRange time: meetingTimes) {
        if (time.contains(event.getWhen())){
          TimeRange firstDivision = TimeRange.fromStartEnd(time.start(), event.getWhen().start(), false);
          TimeRange secondDivision = TimeRange.fromStartEnd(event.getWhen().end(), time.end(), false);
          if (firstDivision.duration() >= requestDuration && firstDivision.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(firstDivision);  
          }
          if (secondDivision.duration() >= requestDuration && secondDivision.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(secondDivision);  
          }
        }
        else if (time.contains(event.getWhen().start())) {
          TimeRange division = TimeRange.fromStartEnd(time.start(), event.getWhen().start(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(division);
          }
        }
        else if (time.contains(event.getWhen().end())) {
          TimeRange division = TimeRange.fromStartEnd(event.getWhen().end(), time.end(), false);
          if (division.duration() >= requestDuration && division.end() <= TimeRange.END_OF_DAY + 1) {
            temp.add(division);  
          }
        }
        else if (!event.getWhen().contains(time)){
          temp.add(time);  
        } 
      }
      meetingTimes = temp;
    }

    return meetingTimes;  
  }

  private Collection<TimeRange> getRequiredTime(Collection<Event> events, long requestDuration) {
    List<TimeRange> meetingTimes = new ArrayList<>();  
    TimeRange initial = TimeRange.WHOLE_DAY;

    if(initial.duration() >= requestDuration) {
      meetingTimes.add(initial);  
    }

    return getTime(events, requestDuration, meetingTimes);
  }

  private Collection<TimeRange> getOptionalTime(Collection<Event> events, long requestDuration, Collection<TimeRange> requiredTimes) {
    return getTime(events, requestDuration, requiredTimes);
  }

  private int eventType(Event event, MeetingRequest request) {
    HashSet<String> attendees = new HashSet<> (event.getAttendees());
    attendees.retainAll(request.getAttendees());  
    if (attendees.size() > 0) {
      return 1;
    }
    HashSet<String> optionalAttendees = new HashSet<> (event.getAttendees());
    optionalAttendees.retainAll(request.getOptionalAttendees());
    if (optionalAttendees.size() > 0) {
        return 2;
    }
    return -1;
  }
}

