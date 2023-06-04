package com.github.rha.storagerest;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class FieldFilterService {

    private Map<String, Chainr> chainrMapCache = new HashMap<>();

    // Default filter only shows partner number
    private static final String defaultFilter = """
                [ {
                    "operation": "shift",
                    "spec": {
                      "employeeNumber": "employeeNumber"
                    }
                  } ]
                """;

    private static final Map<String, String> jsonSpecs = new HashMap<>();
    {
        jsonSpecs.put("abcde", """
                [
                  {
                    "operation": "shift",
                    "spec": {
                      "employeeNumber": "employeeNumber",
                      "firstName": "firstName",
                      "email": "email",
                      "workLocation": {
                        "store": "workLocation.store"
                      }
                    }
                  }
                ]
                """);
        jsonSpecs.put("abcdeall", """
                [ {
                    "operation": "default",
                    "spec": {
                      "filter": "default"
                    }
                  }]
                """);
    }

    public Chainr getCbacFilter(String clientId)  {
        Chainr chainr = chainrMapCache.get(clientId);
        if (chainr == null) {
            var jsonSpecStr = jsonSpecs.getOrDefault(clientId, defaultFilter);
            List<Object> specs = JsonUtils.jsonToList(jsonSpecStr);
            chainr = Chainr.fromSpec(specs);
        }
        chainrMapCache.put(clientId, chainr);
        return chainr;
    }


    public String apply(String clientId, String jsonLine) throws Exception {
        Chainr chainr = getCbacFilter(clientId);
        //  parse
        Object input = JsonUtils.jsonToObject(jsonLine);
        Object transformed = chainr.transform(input);
        return JsonUtils.toJsonString(transformed);
    }
}
