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

/** Class containing server statistics. */
public final class CommentData {

  private final String msg1;
  private final String msg2;
  private final String msg3;

  public CommentData(String firstMessage, String secondMessage, String thirdMessage) {
    this.msg1 = firstMessage;
    this.msg2 = secondMessage;
    this.msg3 = thirdMessage;
  }
}
