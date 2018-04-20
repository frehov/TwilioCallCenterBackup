package no.knowit.fag.callcenter.backup.controller;

import com.twilio.twiml.VoiceResponse;
import no.knowit.fag.callcenter.backup.components.RoutingEngine;
import no.knowit.fag.callcenter.backup.utils.WriteTwiml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@Controller
@Profile("menu")
public class MenuController {

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    private final RoutingEngine routingEngine;

    @Autowired
    public MenuController(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

    @PostMapping("/ivr/welcome/menu")
    public void menu(HttpServletRequest request, HttpServletResponse response) {

        VoiceResponse voiceResponse;
        String menuOption = request.getParameter("Digits");

        log.info(menuOption);

        if(menuOption == null) {
            voiceResponse = routingEngine.getMenu("0");
        } else {
            voiceResponse = routingEngine.getMenu(routingEngine.buildVoiceRoute("0", menuOption));
        }

        log.info(voiceResponse.toString());
        WriteTwiml.write(voiceResponse, response);

    }


    @PostMapping("/ivr/welcome/menu/{submenu}")
    public void entry(HttpServletRequest request, HttpServletResponse response, @PathVariable String submenu) {

        String menuOption = request.getParameter("Digits");

        log.info("targeting menu " + routingEngine.buildVoiceRoute(submenu, menuOption));

        VoiceResponse voiceResponse = routingEngine.getMenu(routingEngine.buildVoiceRoute(submenu, menuOption));

        log.info(voiceResponse != null ? voiceResponse.toString() : null);
        WriteTwiml.write(voiceResponse, response);
    }



}
