# JavaServlet
My first project using Java Servlets.

Currently it only allows creation and deletion of accounts, logging into an existing account, and logging out.

## Storing of Accounts
It stores the Account informations in WEB-INF/accounts/ in a subdirectory "accounts_" followed by a number wich is selected using a hashing algorythm. In the code this number is usually called nameHash. 
In that subdirectory is a file database.data in wich a list of all accounts in that subdirectory, in which always 2 lines are for one account and the first line is for the SHA512 of the name and the second is the filepath including the filename of the account-file.
In the first line of the account-File is the SHA512 of the password nameHash and account id in the subdirectory.

## Sessions
Every logged-in User has an own session with a session id and a session key which are stored in a cookie on the users browser.
In the session is locally stored the unencrypted name, the nameHash, the account id, the session key and the time when the user created the session or last accessed it.
If the time of last access if longer than five minuts ago, the session will expire.
In the code is a limit of 100 session at once coded in but can be changed by simply changing the number in the line of Servlet.java that contains "Session[] session = new Session[100];"

## Updating the webpage
The webpage updates by making a post request to the server containing in the first line of the body the name of the action to make like "login" followed by all relevent informations in the following lines, and then executing the javascript code that is in the reponse of the server, wich usually replaces the inner Html of an HTML element.

## Note
Servlet.java and database.java each have a constant that stores the local directory of "%CATALINA_BASE%/webapps/PROJECT_NAME/WEB-INF" which probably has to be updated.
