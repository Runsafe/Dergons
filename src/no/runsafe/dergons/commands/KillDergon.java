package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.WholeNumber;

public class KillDergon extends ExecutableCommand
{
    public KillDergon(DergonHandler handler)
    {
       super(
           "kill", "Kills a dergon.", "runsafe.dergons.kill",
           new WholeNumber("DergonID").require()
       );
       this.handler = handler;
    }

    @Override
    public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
    {
        int dergonID = parameters.getRequired("DergonID");
        return handler.killDergon(dergonID) ? "&aDergon killed." : "&cDergon could not be killed.";
    }

    private final DergonHandler handler;
}
