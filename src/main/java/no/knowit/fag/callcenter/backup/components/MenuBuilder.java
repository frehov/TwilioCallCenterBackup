package no.knowit.fag.callcenter.backup.components;


import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.twilio.http.HttpMethod.GET;
import static com.twilio.twiml.voice.Gather.Input.DTMF;
import static java.util.stream.Collectors.toList;

@Component
@Profile("menu")
public class MenuBuilder {

    @Autowired
    private MenuConfiguration configuration;

    private Map<String, VoiceResponse> menuMap;

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    @PostConstruct
    public void init() {
        menuMap = new HashMap<>();
        menuMap.put("root", rootMenu());

        for(MenuConfiguration.MenuOption option : configuration.getOptions()) {
            if(option.getOptions() == null) {
                log.info("root: No submenus under \"" + (option.getQueue() != null ? option.getQueue() : option.getRoute()) + "\" adding enqueue command");
                menuMap.put(option.getQueue(), buildResponse(option, true, option.getQueue()));
                //TODO Build an enqueue command and put in map
            } else {
                log.info("root: Recursing submenus under: " + (option.getQueue() != null ? option.getQueue() : option.getRoute()));
                flattenMenu(option.getOptions(), option.getRoute());
            }
        }
        log.info(menuMap.keySet().toString());
        log.info(menuMap.toString());
    }

    private void flattenMenu(List<MenuConfiguration.MenuOption> menulist, String voiceRoute) {
        for(MenuConfiguration.MenuOption option : menulist) {

            if(option.getOptions() == null) {
                //TODO Build Enqueue command and put in map.
                log.info(voiceRoute + ": No submenus under \"" + (option.getQueue() != null ? option.getQueue() : option.getRoute()) + "\" adding enqueue command");
                String route = buildVoiceRoute(voiceRoute, option.getQueue());
                menuMap.put(route, buildResponse(option, true, route));
            } else {
                log.info(voiceRoute+": Recursing submenus under: " + (option.getQueue() != null ? option.getQueue() : option.getRoute()));
                String route = buildVoiceRoute(voiceRoute, option.getRoute());
                menuMap.put(route, buildResponse(option, false, option.getRoute()));
                flattenMenu(option.getOptions(), route);
            }
        }
    }

    private VoiceResponse buildResponse(MenuConfiguration.MenuOption option, boolean isQueue, String menuPath) {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        if(isQueue) {
            builder = builder
                    .enqueue(new Enqueue
                            .Builder(option.getQueue())
                            .waitUrl(configuration.getWaitmusic())
                            .waitUrlMethod(GET)
                            .build());
        } else {
            builder = builder.gather(buildMenu(option.getOptions(), gatherBuilder(menuPath), pause).build());
        }

        return builder.build();
    }

    private String buildVoiceRoute(String basePath, String appendPath) {
        return String.format("%s-%s", basePath, appendPath);
    }

    public VoiceResponse getMenuFromMap(String path) {
        return menuMap.get(path);
    }

    private boolean isSpokenMenu() {
        return configuration.getMenutype().toLowerCase().equals("spoken");
    }

    private List<Say> loadTextMenu(List<MenuConfiguration.MenuOption> options) {
        return options
                .stream()
                .filter(option -> option.getText() != null)
                .map(option -> new Say
                        .Builder(option.getText())
                        .language(configuration.getLanguage())
                        .build())
                .collect(toList());
    }

    private List<Play> loadRecordedMenu(List<MenuConfiguration.MenuOption> options) {
        return options
                .stream()
                .filter(option -> option.getRecording() != null)
                .map(option -> new Play
                        .Builder(option.getRecording())
                        .build())
                .collect(toList());
    }

    private Gather.Builder gatherBuilder(String submenu) {
        return new Gather
                .Builder()
                .action("/ivr/welcome/menu"+( submenu != null ? ("/"+submenu) : "" ))
                .numDigits(1)
                .inputs(DTMF)
                .finishOnKey("#");
    }

    private Gather.Builder buildMenu(List<MenuConfiguration.MenuOption> options, Gather.Builder gatherBuilder, Pause pause) {
        if(isSpokenMenu()) {
            for(Say say : loadTextMenu(options)) {
                gatherBuilder = gatherBuilder.say(say).pause(pause);
            }
        } else {
            for(Play play  : loadRecordedMenu(options)) {
                gatherBuilder = gatherBuilder.play(play).pause(pause);
            }
        }
        return gatherBuilder;
    }

    public VoiceResponse rootMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gatherBuilder = gatherBuilder(null);
        gatherBuilder = buildMenu(configuration.getOptions(), gatherBuilder, pause);

        return builder.gather(gatherBuilder.build()).build();
    }
}
