package no.knowit.fag.callcenter.backup.controller;

import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import no.knowit.fag.callcenter.backup.components.MenuBuilder;
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
    private final VoiceResponse rootMenu;

    @Autowired
    public MenuController(MenuBuilder menuBuilder) {
        this.menuBuilder = menuBuilder;
        this.rootMenu = menuBuilder.rootMenu();
    }

    @PostMapping("/ivr/welcome/menu")
    public void menu(HttpServletRequest request, HttpServletResponse response) {

        String menuOption = request.getParameter("Digits");

        log.info(menuOption);

        if(menuOption == null) {
            try {
                response.setContentType("text/xml;charset=UTF-8");
                response.getWriter().write(rootMenu.toXml());
            } catch (TwiMLException |IOException e){
                throw new RuntimeException(e);
            } finally {
                log.info(rootMenu.toXml());
            }
        } else {

        }
        log.info(rootMenu.toString());
    }


    @PostMapping("/ivr/welcome/menu/{submenu}")
    public void entry(HttpServletRequest request, HttpServletResponse response, @PathVariable String submenu) {

        String menuOption = request.getParameter("Digits");

        log.info(menuOption + " : "+ submenu);

        try {
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write(menuBuilder.getMenuFromMap(submenu).toXml());
        } catch (TwiMLException |IOException e){
            throw new RuntimeException(e);
        } finally {
            log.info(rootMenu.toXml());
        }

    }

}
