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

package com.google.sps.data;

import java.util.*;

/** Class containing the user comments. */
public final class CommentData {

  private final List<String> comments;
<<<<<<< HEAD
  private final List<String> ids;
  private int numComments;

  public CommentData() {
    this.numComments = 0;
    this.comments = new ArrayList<>();
    this.ids = new ArrayList<>();
  }

  public void addComment(String comment, Long id) {
    this.comments.add(comment);
    this.ids.add(String.valueOf(id));
    this.numComments = this.comments.size();
=======

  public CommentData() {
    this.comments = new ArrayList<>();
  }

  public void addComment(String comment) {
    this.comments.add(comment);
>>>>>>> week3-serverStore
  }

  public int getSize() {
    return this.comments.size();
  }

  public String getLast() {
    int lastIndex = getSize() - 1;
    String lastComment = this.comments.get(lastIndex);
    return lastComment;  
  }
}
