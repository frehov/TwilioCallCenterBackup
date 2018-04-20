package no.knowit.fag.callcenter.backup.controller;

import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import no.knowit.fag.callcenter.backup.components.MenuBuilder;
import no.knowit.fag.callcenter.backup.utils.WriteTwiml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Controller
@Profile("menu")
public class MenuController {

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    private final MenuBuilder menuBuilder;

    @Autowired
    public MenuController(MenuBuilder menuBuilder) {
        this.menuBuilder = menuBuilder;
    }

    @PostMapping("/ivr/welcome/menu")
    public void menu(HttpServletRequest request, HttpServletResponse response) {

        VoiceResponse voiceResponse;
        String menuOption = request.getParameter("Digits");

        log.info(menuOption);

        if(menuOption == null) {
            voiceResponse = menuBuilder.getMenuFromMap("0");
        } else {
            voiceResponse = menuBuilder.getMenuFromMap(menuBuilder.buildVoiceRoute("0", menuOption));
        }

        WriteTwiml.write(voiceResponse, response);

        log.info(voiceResponse.toString());
    }


    @PostMapping("/ivr/welcome/menu/{submenu}")
    public void entry(HttpServletRequest request, HttpServletResponse response, @PathVariable String submenu) {

        String menuOption = request.getParameter("Digits");

        log.info("targeting menu " + menuBuilder.buildVoiceRoute(submenu, menuOption));

        VoiceResponse voiceResponse = menuBuilder.getMenuFromMap(menuBuilder.buildVoiceRoute(submenu, menuOption));

        WriteTwiml.write(voiceResponse, response);
    }



}
