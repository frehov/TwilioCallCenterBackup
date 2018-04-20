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

import java.util.List;
import java.util.logging.Logger;

import static com.twilio.twiml.voice.Gather.Input.DTMF;
import static java.util.stream.Collectors.toList;

@Component
@Profile("menu")
public class MenuBuilder {

    @Autowired
    MenuConfiguration configuration;

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());



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
        return configuration.getOptions().stream().filter(x -> x.getValue().equals(option)).findFirst().orElse(configuration.getDummy()).getQueue();
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


    public VoiceResponse finalMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause p = new Pause.Builder().length(configuration.getPause()).build();

        Gather.Builder gBuilder = new Gather.Builder();

        log.info(this.toString());

        for(Say s : loadTextMenu()) {
            builder = builder.say(s).pause(p);
        }

        builder = builder
                .gather(new Gather.Builder()
                        .action("/ivr/welcome/menu/option")
                        .numDigits(1)
                        .inputs(DTMF)
                        .build()
                );

        return builder.build();
    }


}
