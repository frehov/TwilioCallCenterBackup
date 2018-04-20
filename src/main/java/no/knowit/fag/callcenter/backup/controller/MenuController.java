package no.knowit.fag.callcenter.backup.controller;

import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import no.knowit.fag.callcenter.backup.components.MenuBuilder;
import no.knowit.fag.callcenter.backup.components.MenuConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
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
    private final VoiceResponse resp;

    @Autowired
    public MenuController(MenuBuilder menuBuilder) {
        this.menuBuilder = menuBuilder;
        this.resp = menuBuilder.isSpokenMenu() ? menuBuilder.finalSpokenMenu() : menuBuilder.finalPlayedMenu();
    }

    @PostMapping("/ivr/welcome/menu")
    public void menu(HttpServletRequest request, HttpServletResponse response) {

        log.info(resp.toString());

        try {
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write(resp.toXml());
        } catch (TwiMLException |IOException e){
            throw new RuntimeException(e);
        } finally {
            log.info(resp.toXml());
        }

    }


    @PostMapping("/ivr/welcome/menu/{menuType}")
    public void entry(HttpServletRequest request, HttpServletResponse response) {
        String menuOption = request.getParameter("Digits");

        log.info(menuOption);

        String queue = menuBuilder.getQueue(menuOption);



        log.info(queue);

    }

}
