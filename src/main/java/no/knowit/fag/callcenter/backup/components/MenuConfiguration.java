package no.knowit.fag.callcenter.backup.components;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Pause;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Say.Language;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.twilio.twiml.voice.Gather.Input.DTMF;
import static java.util.stream.Collectors.toList;

@Component
@Profile("menu")
@ConfigurationProperties(prefix = "ivr.menu")
@Getter @Setter
public class MenuConfiguration {
    private List<Map<String, String>> options;
    private Language language;
    private String default_queue;
    private int pause;

    private final Logger log = Logger.getLogger(this.getClass().toGenericString());

    private Map<String,String> dummy = new HashMap();
    {dummy.put("queue", getDefault_queue());}

    private List<Say> loadConfiguredMenu() {
        return getOptions()
                .stream()
                .filter(option -> option.containsKey("text"))
                .map(option -> new Say
                        .Builder(option.get("text"))
                        .language(language)
                        .build())
                .collect(toList());
    }

    public String getQueue(String option) {
        return getOptions().stream().filter(x -> x.containsValue(option)).findFirst().orElse(dummy).get("queue");
    }

    public VoiceResponse finalMenu() {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        Pause p = new Pause.Builder().length(pause).build();

        Gather.Builder gBuilder = new Gather.Builder();

        log.info(getOptions().toString());

        for(Say s : loadConfiguredMenu()) {
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
