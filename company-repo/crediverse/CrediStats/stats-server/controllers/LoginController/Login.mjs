import fetch from "node-fetch";
import crypto from "crypto";
import jwt from "jsonwebtoken";

console.log("Logging in to Crediverse");

import { SECRET_KEY } from "../../verifyToken.mjs";

async function createNewSession() {
  var loginRequestGetSessionId = {
    version: "1",
    mode: "N",
    companyID: 2,
    channel: "W",
    ipAddress: "127.0.0.1",
    userType: "WEBUSER",
  };

  let getSessionIdResponse = await fetch(
    "http://localhost:14400/ecds/authentication/authenticate",
    {
      method: "post",
      headers: {
        Accept: "application/json, application/*+json",
        Connection: "keep-alive",
        "Content-Type": "application/json",
        Host: "localhost:14400",
        "User-Agent": "Apache-HttpClient/4.5.13 (Java/11.0.19)",
        "Accept-Encoding": "gzip,deflate",
      },
      body: JSON.stringify(loginRequestGetSessionId),
    }
  );

  let responseJson = await getSessionIdResponse.json();

  let sessionId = responseJson.sessionID;
  return sessionId;
}

async function submitUserName(username, sessionId) {
  let buff = Buffer.from(username);

  var loginRequestSubmitBase64Username = {
    sessionID: sessionId,
    version: "1",
    mode: "N",
    companyID: 2,
    channel: "W",
    ipAddress: "0:0:0:0:0:0:0:1",
    userType: "WEBUSER",
    data: buff.toString("base64"),
  };
  console.log(
    "loginRequestSubmitBase64UserName:" +
      JSON.stringify(loginRequestSubmitBase64Username)
  );

  let rawSubmitUsernameResponse = await fetch(
    "http://localhost:14400/ecds/authentication/authenticate",
    {
      method: "post",
      headers: {
        CS_SID: sessionId,
        Accept: "application/json, application/*+json",
        Connection: "keep-alive",
        "Content-Type": "application/json",
        Host: "localhost:14400",
        "User-Agent": "Apache-HttpClient/4.5.13 (Java/11.0.19)",
        "Accept-Encoding": "gzip,deflate",
      },
      body: JSON.stringify(loginRequestSubmitBase64Username),
    }
  );

  let submitUsernameResponse = await rawSubmitUsernameResponse.json();

  console.log(
    "submitUserNameResponse: " + JSON.stringify(submitUsernameResponse)
  );

  let key = submitUsernameResponse.key1;
  var pem = "-----BEGIN PUBLIC KEY-----\n" + key + "\n-----END PUBLIC KEY-----";

  return pem;
}

async function submitPassword(password, key, sessionId) {
  let rawEncryptedPassword = await crypto.publicEncrypt(
    {
      key: key,
      padding: crypto.constants.RSA_PKCS1_PADDING,
    },
    Buffer.from(password, "utf8")
  );

  let encryptedPassword = rawEncryptedPassword.toString("base64");

  let loginRequestSubmitPassword = {
    sessionID: sessionId,
    version: "1",
    mode: "N",
    companyID: 2,
    channel: "W",
    ipAddress: "0:0:0:0:0:0:0:1",
    userType: "WEBUSER",
    data: encryptedPassword,
  };

  console.log(
    "loginRequestSubmitPassword:" + JSON.stringify(loginRequestSubmitPassword)
  );

  let rawSubmitPasswordResponse = await fetch(
    "http://localhost:14400/ecds/authentication/authenticate",
    {
      method: "post",
      headers: {
        CS_SID: sessionId,
        Accept: "application/json, application/*+json",
        Connection: "keep-alive",
        "Content-Type": "application/json",
        Host: "localhost:14400",
        "User-Agent": "Apache-HttpClient/4.5.13 (Java/11.0.19)",
        "Accept-Encoding": "gzip,deflate",
      },
      body: JSON.stringify(loginRequestSubmitPassword),
    }
  );


  let submitPasswordResponse = await rawSubmitPasswordResponse.json();

  console.log(submitPasswordResponse);
  
  return submitPasswordResponse.returnCode;
}

const login = async (request, response) => {
  try {
    let request_body = request.body;

    console.log("Request Body: " + JSON.stringify(request_body));

    let sessionId = await createNewSession();
    console.log("sessionId: " + sessionId);

    let username = request_body.username;
    let password = request_body.password;

    let key = await submitUserName(username, sessionId);

    let submitPasswordResponse = await submitPassword(password, key, sessionId);

    console.log(
      "********************Crediverse Auth Response ************************"
    );
    console.log(submitPasswordResponse);

    if (submitPasswordResponse === "AUTHENTICATED" || submitPasswordResponse === "REQUIRE_UTF8_OTP") {
      let tokenExpiryTime = "2h";
      let jwtSecret = SECRET_KEY;

      const token = await jwt.sign(
        {
          username: username,
          sessionId: sessionId,
        },
        jwtSecret,
        {
          expiresIn: tokenExpiryTime,
        }
      );

      let loginResponse = {
        accessToken: token,
        expiresIn: tokenExpiryTime,
      };

      response.json(loginResponse);
    } else {
      console.log("HTTP 401 returned: ",submitPasswordResponse);
      response.status(401).json({ error: submitPasswordResponse });
    }
  } catch (error) {
    console.error(error);
    response.status(500).json({ error: error });
  }
};

export { login };
