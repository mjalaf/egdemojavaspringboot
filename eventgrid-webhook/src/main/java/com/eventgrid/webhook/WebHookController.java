package com.eventgrid.webhook;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins="*") 
public class WebHookController {

    @PostMapping(value = "/webhook", produces = "application/json")
    public ResponseEntity<Map<String,String>> handleWebhook(@RequestBody String payload) {
        // Validate the payload
      //  if (!isValidPayload(payload)) {
        //    return ResponseEntity.badRequest().body("Invalid payload");
       // }

        // Process the payload
        processPayload(payload);
        ResponseEntity.status(200);

        String validationCode = "00000000000";

        try {
          validationCode = getValidateionCode(payload);

        } catch (Exception e) {
          System.out.println("Validation Code Empty");
        }
        
        HashMap<String,String> map = new HashMap<>();
        // return json
        map.put("validationResponse", validationCode);

        return ResponseEntity.ok(map);
    }

    //private boolean isValidPayload(String payload) {
        // Validate the payload
      //  return true;
    //}

    private String getValidateionCode(String payload) {
         JSONArray jarrayData = new JSONArray(payload);

        JSONObject jo = (JSONObject) jarrayData.get(0);
     

        JSONObject jsonDataRaw = (JSONObject) jo.get("data");

        return jsonDataRaw.getString("validationCode");

    }

    private void processPayload(String payload) {
        System.out.println("Processing payload: " + payload);
    }
}
