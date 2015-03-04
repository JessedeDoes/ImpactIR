package nl.namescape.tagging;

import nl.openconvert.filehandling.SimpleInputOutputProcess;

public interface TaggerWithOptions extends SimpleInputOutputProcess 
{
	public void setTokenizing(boolean b);
}
