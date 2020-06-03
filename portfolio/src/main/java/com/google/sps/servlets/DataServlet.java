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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private int num_cmnts = 0;
  private ArrayList<String> comments;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /*These lines used to return a Hello Aymar message*/
    //response.setContentType("text/html;");
    //response.getWriter().println("Hello Aymar!");

    /*These lines were used to send a json string of messages*/
    /*ArrayList<String> msg = new ArrayList<String>();
    msg.add("First message.");
    msg.add("This is the second string.");
    msg.add("Last string.");
    String json = convertToJson(msg);
    response.setContentType("application/json;");
    response.getWriter().println(json);*/

    comments = new ArrayList<String>();
    Query query = new Query("Comment").addSort("Number", SortDirection.ASCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    num_cmnts = 0;

    for (Entity entity : results.asIterable()) {
      num_cmnts++;
      String text = (String) entity.getProperty("text");

      comments.add("#Comment"+num_cmnts+": "+text);
    }

    response.setContentType("application/json");
    String json = convertComments();
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    num_cmnts++;
    String Cmt = request.getParameter("cmnt");

    Entity taskEntity = new Entity("Comment");
    taskEntity.setProperty("Number", num_cmnts);
    taskEntity.setProperty("text", Cmt);
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    response.sendRedirect("/index.html");
  }

  /**
   * Converts an ArrayList into a JSON string using manual String concatentation.
   */
  private String convertToJson(ArrayList<String> msg) {
    String json = "{";
    int size = msg.size();
    for(int i =0; i< size; i++){
        json += "\"msg"+(i+1)+"\": ";
        json += "\""+msg.get(i)+"\"";
        if(i!=size-1){
            json += ", ";
        }
    }
    json += "}";
    return json;
  }

  private String convertComments() {
      String json = "{";
      json+="\"num_comments\": " + num_cmnts;
      json+=", \"comments\": [";
      for(int i=0; i < num_cmnts; i++) {
          json += "\"" + comments.get(i) + "\"";
          if(i!=num_cmnts-1){
              json+=", ";
          }
      }
      json+="]}";
      return json;
  }
}
