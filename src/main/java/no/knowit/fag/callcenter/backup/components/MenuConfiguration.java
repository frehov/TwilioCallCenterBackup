package no.knowit.fag.callcenter.backup.components;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Pause;
import com.twilio.twiml.voice.Play;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Say.Language;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.logging.Logger;

import static com.twilio.twiml.voice.Gather.Input.DTMF;
import static java.util.stream.Collectors.toList;

@Component
@Profile("menu")
@ConfigurationProperties(prefix = "ivr.menu")
@Data
@Validated
public class MenuConfiguration {

    @NotNull
    private List<MenuOption> options;

    @NotNull
    private Language language;

    @NotNull
    private String default_queue;

    @NotNull
    private int pause;

    @NotNull
    private String waitmusic;

    @Data
    @Valid
    private static class MenuOption {
        private String text;
        private String recorded_text;

        @NotNull
        private String queue;

        @NotNull
        private String value;

        private List<MenuOption> options;
    }

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    private MenuOption dummy = new MenuOption();
    {dummy.setQueue(getDefault_queue());}

    private List<Say> loadTextMenu() {
        return getOptions()
                .stream()
                .filter(option -> option.getText() != null)
                .map(option -> new Say
                        .Builder(option.getText())
                        .language(language)
                        .build())
                .collect(toList());
    }


    private List<Play> loadRecordedMenu() {
        return getOptions()
                .stream()
                .filter(option -> option.getRecorded_text() != null)
                .map(option -> new Play
                        .Builder(option.getRecorded_text())
                        .build())
                .collect(toList());
    }

    public String getQueue(String option) {
        return getOptions().stream().filter(x -> x.getValue().equals(option)).findFirst().orElse(dummy).getQueue();
    }


    public VoiceResponse finalMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause p = new Pause.Builder().length(pause).build();

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
