package no.knowit.fag.callcenter.backup.components;


import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Pause;
import com.twilio.twiml.voice.Play;
import com.twilio.twiml.voice.Say;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
                //TODO Build an enqueue command
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

    private String buildVoiceRoute(String basePath, String appendPath) {
        return String.format("%s-%s", basePath, appendPath);
    }

    public boolean isSpokenMenu() {
        return configuration.getMenutype().toLowerCase().equals("spoken");
    }

    private List<Say> loadTextMenu() {
        return configuration.getOptions()
                .stream()
                .filter(option -> option.getText() != null)
                .map(option -> new Say
                        .Builder(option.getText())
                        .language(configuration.getLanguage())
                        .build())
                .collect(toList());
    }


    private List<Play> loadRecordedMenu() {
        return configuration.getOptions()
                .stream()
                .filter(option -> option.getRecorded_text() != null)
                .map(option -> new Play
                        .Builder(option.getRecorded_text())
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

    public VoiceResponse finalPlayedMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gatherBuilder = gatherBuilder("played");

        for(Play play  : loadRecordedMenu()) {
            gatherBuilder = gatherBuilder.play(play).pause(pause);
        }

        return builder.gather(gatherBuilder.build()).build();
    }

    public VoiceResponse finalSpokenMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause pause = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gatherBuilder = gatherBuilder("spoken");

        for(Say s : loadTextMenu()) {
            gatherBuilder = gatherBuilder.say(s).pause(pause);
        }

        return builder.gather(gatherBuilder.build()).build();
    }

}
