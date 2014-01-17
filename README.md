mhswsstackapi-gen2rms
=====================

Microhouse Web Services Stack API (for gen2/RMS) is provided here to help our customers use our "basic" API for our RMS stack. This is the gen2 iteration, and we have already sarted gen3.

The RMS stack is RESTful API. It uses simple HTTPS POST methods to access the RMS backend (or other backends we have connected it to, such as our ERP solutions). It hides the complexity of our backend and buffers the developer from worrying about our model (and constant changes to it).

The stack is divided into five categories of functions, namely:
Query API;
Status API;
Update API;
Reporting API;
Administration API.
I'll be discussing these in detail in other posts to this project. All these calls have certain basic requirements. 

Method

Save for functions that belong to the Reporting API, the stack supports POST method only.

HTTPS

Although the stack is open to HTTP at the moment, in production mode, it will only respond to HTTPS requests.

Encoding

Everything is UTF-8 encoded.

Authentication

You will be assigned a client access identifier and a client secret key. Keep these safe.

When forming any request, you need to set two HTTPS headers. These are used to authenticate your request and relate it to your account and your backend RMS instance.

These headers are x-client-identifier and x-payload-signature.

The x-client-identifier is to be set to the value of the client access identifier assigned to you.

The x-payload-signature is the MD5 calculation of the client secret key assigned to you concatenated to the payload string.

In other words, x-payload-signature = MD5 of ("client secret key" + "payload string").

I have attached a small sample program with test keys that demonstrates some of these calls. The code is written in Java, but it is very easy to follow.

Format

All data passed back end forth is in JSON format.

Status Codes and Error Handling

All successful calls with have a return code of 200. The JSON result will contain status 0. Errors will have a return code of 400 (or 500) and the status, if present, will not be 0. Status codes will be discussed in detail.
