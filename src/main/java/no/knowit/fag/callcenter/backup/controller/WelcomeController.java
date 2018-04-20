package no.knowit.fag.callcenter.backup.controller;

import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Redirect;
import no.knowit.fag.callcenter.backup.components.MenuConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import static com.twilio.twiml.voice.Say.Language.*;

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

        try {
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write(resp.toXml());
        } catch (TwiMLException|IOException e){
            throw new RuntimeException(e);
        } finally {
            log.info(resp.toXml());
        }
    }
}
