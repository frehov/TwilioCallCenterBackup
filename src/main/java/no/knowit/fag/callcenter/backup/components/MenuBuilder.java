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
        menuMap.put("spoken", finalSpokenMenu());
        menuMap.put("played", finalPlayedMenu());

        for(MenuConfiguration.MenuOption option : configuration.getOptions()) {
            if(option.getOptions() == null) {
                //TODO Build an enqueue command and put in map
            } else {
                flattenMenu(option.getOptions(), option.getRoute());
            }
        }
        log.info(menuMap.toString());
    }

    private void flattenMenu(List<MenuConfiguration.MenuOption> menulist, String voiceRoute) {
        for(MenuConfiguration.MenuOption option : menulist) {

            if(option.getOptions() == null) {
                //TODO Build Enqueue command and put in map.
            } else {
                menuMap.put(buildVoiceRoute(voiceRoute, option.getQueue()), null);
                flattenMenu(option.getOptions(),  buildVoiceRoute(voiceRoute,option.getRoute()));
            }
        }
    }

    private VoiceResponse buildResponse(MenuConfiguration.MenuOption option, boolean isQueue) {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();

        return new VoiceResponse.Builder()
                .enqueue(new Enqueue.Builder()
                        .waitUrl(configuration.getWaitmusic())
                        .waitUrlMethod(GET)
                        .build())
                .build();
    }

    private String buildVoiceRoute(String basePath, String appendPath) {
        return String.format("%s-%s", basePath, appendPath);
    }

    public boolean isSpokenMenu() {
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

    public String getQueue(String option) {
        return configuration.getOptions()
                .stream()
                .filter(x -> x.getValue().equals(option))
                .findFirst()
                .orElse(configuration.getDummy())
                .getQueue();
    }

    private Gather.Builder gatherBuilder(String menuType) {
        return new Gather.Builder()
                .action("/ivr/welcome/menu/"+menuType)
                .numDigits(1)
                .inputs(DTMF)
                .finishOnKey("#");
    }

    public VoiceResponse rootMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gatherBuilder = gatherBuilder(isSpokenMenu()?"spoken":"played");

        if(isSpokenMenu()) {
            for(Say s : loadTextMenu(configuration.getOptions())) {
                gatherBuilder = gatherBuilder.say(s).pause(pause);
            }
        } else {
            for(Play play  : loadRecordedMenu(configuration.getOptions())) {
                gatherBuilder = gatherBuilder.play(play).pause(pause);
            }
        }

        return builder.gather(gatherBuilder.build()).build();
    }

    public VoiceResponse finalPlayedMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gatherBuilder = gatherBuilder("played");

        for(Play play  : loadRecordedMenu(configuration.getOptions())) {
            gatherBuilder = gatherBuilder.play(play).pause(pause);
        }

        return builder.gather(gatherBuilder.build()).build();
    }

    public VoiceResponse finalSpokenMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gatherBuilder = gatherBuilder("spoken");

        for(Say s : loadTextMenu(configuration.getOptions())) {
            gatherBuilder = gatherBuilder.say(s).pause(pause);
        }

        return builder.gather(gatherBuilder.build()).build();
    }

}
