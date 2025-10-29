package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.InetAddress;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class DashboardController {

    @GetMapping("/app")
    public String app(Model model) throws Exception {
        model.addAttribute("message", "Deployed via Jenkins → Docker → EKS");
        model.addAttribute("time", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        model.addAttribute("host", InetAddress.getLocalHost().getHostName());
        model.addAttribute("pod", getenv("POD_NAME"));
        model.addAttribute("namespace", getenv("POD_NAMESPACE"));
        model.addAttribute("node", getenv("NODE_NAME"));
        model.addAttribute("appBuild", getenv("APP_BUILD"));
        model.addAttribute("appImage", getenv("APP_IMAGE"));
        return "dashboard";
    }

    @RestController
    @RequestMapping("/api")
    static class Api {
        @GetMapping("/info")
        public Map<String, Object> info() throws Exception {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("message", "Deployed via Jenkins → Docker → EKS");
            m.put("time", ZonedDateTime.now().toString());
            m.put("host", InetAddress.getLocalHost().getHostName());
            m.put("pod", getenv("POD_NAME"));
            m.put("namespace", getenv("POD_NAMESPACE"));
            m.put("node", getenv("NODE_NAME"));
            m.put("appBuild", getenv("APP_BUILD"));
            m.put("appImage", getenv("APP_IMAGE"));
            return m;
        }
    }

    private static String getenv(String k) {
        String v = System.getenv(k);
        return v == null ? "" : v;
    }
}
