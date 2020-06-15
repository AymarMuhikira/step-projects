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

package com.google.sps.servlets;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.sps.data.CommentData;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private CommentData commentData = new CommentData();    

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int maxComment = 0;  
    String maxCommentstring = request.getParameter("max-comment");

    try {
      maxComment = Integer.parseInt(maxCommentstring);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert string " + maxCommentstring + " to int");
      maxComment = 1;
    }

    commentData = new CommentData();  

    Query query = new Query("Comment").addSort("number", SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      int commentNumber = commentData.getSize() + 1;

      if (commentNumber <= maxComment) {
        String text = (String) entity.getProperty("text");
        long id = entity.getKey().getId();
        String score = (String) entity.getProperty("score");

        commentData.addComment("Comment#" + commentNumber + ": " + text + "(" + score + ")", id);
      }  
    }

    response.setContentType("application/json");
    String json = convertToJson(commentData);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("cmnt");

    Document doc = Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();

    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("number", commentData.getSize() + 1);
    taskEntity.setProperty("text", comment);
    taskEntity.setProperty("score", String.valueOf(score));
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    response.sendRedirect("/index.html");
  }

  /**
   * Converts an ArrayList into a JSON string using Gson library
   */
  private String convertToJson(CommentData msg) {
    Gson gson = new Gson();
    String json = gson.toJson(msg);
    return json;
  }
}
