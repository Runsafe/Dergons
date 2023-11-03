package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.BooleanArgument;
import no.runsafe.framework.api.command.argument.IArgumentList;

public class ShowBarID extends ExecutableCommand
{
	protected ShowBarID(DergonHandler handler)
	{
		super(
			"barID", "Shows a dergons ID on their boss bar.", "runsafe.dergons.id",
			new BooleanArgument("showBarID").require()
		);
		this.handler = handler;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		boolean showBarID = parameters.getRequired("showBarID");
		handler.setShowBossBarID(showBarID);
		if (showBarID)
			return "&aShowing dergon IDs on their boss bars.";
		else
			return "&aNo longer showing dergon IDs on their boss bars.";
	}

	private final DergonHandler handler;
}
