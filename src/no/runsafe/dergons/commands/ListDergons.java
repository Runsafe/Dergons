package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ListDergons extends ExecutableCommand
{
    public ListDergons(DergonHandler handler)
    {
        super("list", "Gives a list of all current dergons.", "runsafe.dergons.list");
        this.handler = handler;
    }

    @Override
    public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
    {
        List<String> dergonListInfo = handler.getAllDergonInfo();
        if (dergonListInfo.isEmpty())
            return "&cNo dergons found.";

        return String.format(
            "&a&l%d Dergon(s) Located:&r\n %s", dergonListInfo.size(),
            StringUtils.join(dergonListInfo, "\n  ")
        );
    }

    private final DergonHandler handler;
}
