package com.atlassian.jgitflow.core;

import com.google.common.base.Strings;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Config.ConfigEnum;
import org.eclipse.jgit.lib.ConfigConstants;

public enum CoreEol implements ConfigEnum
{
    LF("lf", "\n"),
    CRLF("crlf", "\r\n"),
    NATIVE("native", System.getProperty("line.separator"));

    public static CoreEol DEFAULT = NATIVE;
    public static final String CONFIG_KEY_EOL = "eol";

    private final String configValue;
    private final String eol;

    private CoreEol(final String configValue, final String eol)
    {
        this.configValue = configValue;
        this.eol = eol;
    }

    public String getEol()
    {
        return eol;
    }

    public static CoreEol getConfigValue(final Config gitConfig)
    {
        return
                gitConfig.getEnum(CoreEol.values(),
                        ConfigConstants.CONFIG_CORE_SECTION,
                        null,
                        CONFIG_KEY_EOL,
                        DEFAULT);
    }

    public static CoreEol fromString(String eol)
    {
        if(!Strings.isNullOrEmpty(eol))
        {
            for(CoreEol type : CoreEol.values())
            {
                if(type.toConfigValue().equalsIgnoreCase(eol))
                {
                    return type;
                }
            }
        }

        return null;
    }

    public static boolean isValid(String eol)
    {
        return (null != fromString(eol));
    }
    
    @Override
    public String toConfigValue()
    {
        return configValue;
    }

    @Override
    public boolean matchConfigValue(final String in)
    {
        return configValue.equalsIgnoreCase(in);
    }
}
