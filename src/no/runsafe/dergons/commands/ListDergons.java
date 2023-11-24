package no.runsafe.dergons.commands;

import no.runsafe.dergons.DergonHandler;
import no.runsafe.dergons.DergonHolder;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.player.IPlayer;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
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
        List<String> dergonListInfo = getAllDergonInfo();
        if (dergonListInfo.isEmpty())
            return "&cNo dergons found.";

        return String.format(
            "&a&l%d Dergon(s) Located:&r\n %s", dergonListInfo.size(),
            StringUtils.join(dergonListInfo, "\n  ")
        );
    }

	public List<String> getAllDergonInfo()
	{
		List<String> info = new ArrayList<>();
		HashMap<Integer, DergonHolder> activeDergons = handler.getActiveDergons();

		for (Integer id : activeDergons.keySet())
		{
			DergonHolder dergon = activeDergons.get(id);
			boolean isUnloaded = dergon.isUnloaded();
			boolean isNull = !dergon.isHoldingDergon();

			if (!isNull && !isUnloaded)
			{
				ILocation spawnLocation = dergon.getSpawnLocation();
				ILocation dergonLocation = dergon.getLocation();
				ILocation targetDestination = dergon.getTargetFlyToLocation();
				IPlayer target = dergon.getCurrentTarget();
				info.add(
					"&5ID: &r " + id +
					", &9Health: &r (" + dergon.getHealth() + "/" + dergon.getMaxHealth() + ")" +
					", \n&9Target: &r " + target.getPrettyName() +
					", \n&9SpawnLocation: &r" + locationInfo(spawnLocation) +
					", \n&9Location: &r" + locationInfo(dergonLocation) +
					", \n&9Destination: &r" + locationInfo(targetDestination)
				);
			}
			else if (isUnloaded)
			{
				ILocation unloadLocation = dergon.getUnloadLocation();
				info.add(
					"&5ID: &r " + id +
					", &9Health: &r (" + dergon.getHealth() + "/" + dergon.getMaxHealth() + "), &cUnloaded Dergon&r" +
					"\n&9UnloadLocation: &r" + locationInfo(unloadLocation)
				);
			}
			else info.add("&5ID: &r " + id +", &4Null Dergon.");
		}
		return info;
	}

	private String locationInfo(ILocation location)
	{
		if (location == null)
			return "&cN/A&r";

		return String.format(
				"&eWorld:&r %s &eX:&r %.0f &eY:&r %.0f &eZ:&r %.0f",
				location.getWorld().getName(), location.getX(), location.getY(), location.getZ()
		);
	}

    private final DergonHandler handler;
}
