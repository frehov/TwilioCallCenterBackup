package no.knowit.fag.callcenter.backup.controller;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Play;
import com.twilio.twiml.voice.Redirect;
import com.twilio.twiml.voice.Say;
import no.knowit.fag.callcenter.backup.components.MenuConfiguration;
import no.knowit.fag.callcenter.backup.components.RoutingEngine;
import no.knowit.fag.callcenter.backup.extras.enums.CallType;
import no.knowit.fag.callcenter.backup.utils.WriteTwiml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Controller
public class StatusController {

    @Autowired
    private RoutingEngine routingEngine;

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    @PostMapping("/ivr/status/{callType}/{id}")
    public void queueStatus(HttpServletRequest request, HttpServletResponse response, @PathVariable CallType callType, @PathVariable String id) {


        Map<String, String[]> parameters = request.getParameterMap();
        Set<String> parameterNames = request.getParameterMap().keySet();

        log.info(parameterNames.toString());
        for (String parameter : parameterNames) {
            log.info(parameter + " : " + request.getParameter(parameter));
        }

        switch (callType) {
            case queue:
                WriteTwiml.write(new VoiceResponse.Builder()
                        .say(new Say.Builder("Du er nummer " + request.getParameter("QueuePosition") + " i køen " + id)
                                .language(routingEngine.getConfiguration().getLanguage())
                                .build())
                        .play(new Play.Builder(routingEngine.getConfiguration().getWaitmusic())
                                .build())
                        .build(), response);
                break;
            case conference:
                break;
            default:
                WriteTwiml.write(new VoiceResponse.Builder()
                        .redirect(new Redirect.Builder("ivr/welcome/menu")
                                .build())
                        .build(), response);
        }
    }

    @PostMapping("/ivr/status/{callType}")
    public void dialStatus(HttpServletRequest request, HttpServletResponse response, @PathVariable CallType callType) {
    }

}
