package no.knowit.fag.callcenter.backup.controller;

import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Redirect;
import no.knowit.fag.callcenter.backup.components.MenuConfiguration;
import no.knowit.fag.callcenter.backup.utils.WriteTwiml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Controller
public class WelcomeController {

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    @Autowired
    MenuConfiguration config;

    @PostMapping("/ivr/welcome")
    public void welcome(HttpServletRequest request, HttpServletResponse response) {
        Say say  = new Say
                .Builder("Hei Henrik, Har du det bra?")
                .language(config.getLanguage())
                .build();

        Redirect menu = new Redirect
                .Builder("/ivr/welcome/menu")
                .build();

        VoiceResponse resp = new VoiceResponse
                .Builder()
                .say(say)
                .redirect(menu)
                .build();

        WriteTwiml.write(resp, response);
    }
}
