package nl.namescape.tagging;

import nl.namescape.filehandling.SimpleInputOutputProcess;

public interface TaggerWithOptions extends SimpleInputOutputProcess 
{
	public void setTokenizing(boolean b);
}
