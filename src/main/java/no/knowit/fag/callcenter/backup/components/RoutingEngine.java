package no.knowit.fag.callcenter.backup.components;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.*;
import com.twilio.twiml.voice.Number;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.logging.Logger;

import static com.twilio.http.HttpMethod.GET;
import static com.twilio.twiml.voice.Gather.Input.DTMF;
import static java.util.stream.Collectors.toList;

@Component
@Profile("menu")
public class RoutingEngine {

    @Autowired
    private MenuConfiguration configuration;

    private Map<String, VoiceResponse> menuMap;

    private Set<String> availableQueues;

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    @PostConstruct
    public void init() {
        availableQueues = new HashSet<>();
        menuMap = new HashMap<>();
        menuMap.put("0", rootMenu());

        flattenMenu(configuration.getOptions(), "0");

        log.info("Flattened menu = "+menuMap.keySet().toString());
        log.info("Mapped available queues = "+availableQueues.toString());
    }

    private void flattenMenu(List<MenuConfiguration.MenuOption> menulist, String voiceRoute) {
        for(MenuConfiguration.MenuOption option : menulist) {
            String route = buildVoiceRoute(voiceRoute, option.getValue());
            boolean isLeafNode = getLeafNodeStatus(option);

            if(option.getOptions() == null) {
                log.info(voiceRoute + ": No submenus under option \"" + option.getValue() + "\" adding leaf node");
                if(isLeafNode) {
                    menuMap.put(route, buildResponse(option, isLeafNode, route));
                    continue;
                }
                log.info(route + ": Leaf Node has no \"dial\" or \"queue\" command");
            } else {
                log.info(voiceRoute+": Recursing submenus under option: " + option.getValue());
                menuMap.put(route, buildResponse(option, isLeafNode, route));
                flattenMenu(option.getOptions(), route);
            }
        }
    }

    private boolean getLeafNodeStatus(MenuConfiguration.MenuOption option){
        return option.getQueue() != null || option.getDial()!= null;
    }

    private VoiceResponse buildResponse(MenuConfiguration.MenuOption option, boolean isLeafNode, String menuPath) {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        if(isLeafNode) {
            if(option.getQueue()!= null) {
                availableQueues.add(option.getQueue());
                log.info(menuPath + ": Registering queue \"" + option.getQueue() + "\"");
                builder = builder
                        .enqueue(new Enqueue
                                .Builder(option.getQueue())
                                .waitUrl(configuration.getWaitmusic())
                                .waitUrlMethod(GET)
                                .build());
            } else if(option.getDial() != null) {
                for(String number : option.getDial()) {
                    log.info(menuPath + ": Registering " + number + " to dialing pool");
                    builder = builder
                            .dial(new Dial
                                    .Builder()
                                    .number(new Number
                                            .Builder(number)
                                            .build())
                                    .build());
                }
            }
        } else {
            builder = builder.gather(buildMenu(option.getOptions(), gatherBuilder(menuPath), pause).build());
        }

        return builder.build();
    }

    public String buildVoiceRoute(String basePath, String subPath) {
        return String.format("%s-%s", basePath, subPath);
    }

    public VoiceResponse getMenu(String path) {
        return menuMap.getOrDefault(path, menuMap.get("0--1"));
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

    private VoiceResponse rootMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gatherBuilder = gatherBuilder("0");
        gatherBuilder = buildMenu(configuration.getOptions(), gatherBuilder, pause);

        return builder.gather(gatherBuilder.build()).build();
    }
}
