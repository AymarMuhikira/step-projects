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

/**
 * Adds a random fact about me.
 */
function addRandomFact() {
  const facts =
      ['I am new to web dev', 'I am an EE major', 'I speak french'];

  // Pick a random fact.
  const randomFact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('random-fact-container');
  factContainer.innerText = randomFact;
}

/**
 * Adds a greeting to me.
 */
async function getHello() {
  const response = await fetch('/data');
  const hello_msg = await response.text();
  document.getElementById('greeting-container').innerText = hello_msg;
}

/**
 *Adds json messages to the message division
 */
async function getMessage() {
  const response = await fetch('/data');
  const json = await response.json();
  const message = json.firstMessage + " " + json.secondMessage + " " + json.thirdMessage;
  document.getElementById('message-container').innerText = message;
}

/**
 *Gets the comments posted and prints them back
 */
async function getComments() {
  var maxComments = document.getElementById('max-comment').value;
  const response = await fetch('/data?max-comment=' + maxComments);
  const json = await response.json();
  const postedComments = document.getElementById('posted_comments');
  postedComments.innerHTML = "";
  const numComments = json.numComments;
  const comments = json.comments;
  for (var i = 0; i < numComments; i++) {
    const ids = json.ids;

    const liElement = document.createElement('li');
    liElement.className = 'comment';
    
    const textElement = document.createElement('span');
    textElement.innerText = comments[i];

    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.innerText = 'Delete';
    const id = ids[i];

    deleteButtonElement.value = id;
    deleteButtonElement.addEventListener('click', () => {
      deleteTask(deleteButtonElement.value);

      // Remove the task from the DOM.
      liElement.remove();
    });

    liElement.appendChild(textElement);
    liElement.appendChild(deleteButtonElement);

    postedComments.appendChild(liElement);
  }
}

/** Calls server to delete the task. */
function deleteTask(id) {
  const params = new URLSearchParams();
  params.append('id', id);
  fetch('/delete-comment', {method: 'POST', body: params});
}
