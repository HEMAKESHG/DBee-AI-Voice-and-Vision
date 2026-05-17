import express from "express";
import bodyParser from "body-parser";
import dialogflow from "@google-cloud/dialogflow";
import { v4 as uuidv4 } from "uuid";
import cors from "cors";
import fs from "fs";

const app = express();
app.use(cors());
app.use(bodyParser.json());

const sessionClient = new dialogflow.SessionsClient();


app.post("/query", async (req, res) => {
  try {
    console.log("Request body:", req.body);

    const text = req.body.message || req.body.query;
    console.log("User said:", text);

    const sessionPath = sessionClient.projectAgentSessionPath(
      "hemakesh-qiwp",  // ✅ your project ID
      uuidv4()
    );

    const request = {
      session: sessionPath,
      queryInput: {
        text: {
          text,
          languageCode: "en",
        },
      },
    };

    const [response] = await sessionClient.detectIntent(request);
    const reply = response.queryResult.fulfillmentText;

    console.log("Bot replied:", reply);
    res.json({ reply });
  } catch (error) {
    console.error("Dialogflow Error:", error);
    res.status(500).json({ reply: "Sorry, something went wrong." });
  }
});
console.log("Key exists:", fs.existsSync("service-account.json"));
app.listen(3000, () => console.log("✅ Server running on http://localhost:3000"));
