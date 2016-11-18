package web.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Param {

	protected static final Logger logger = LoggerFactory.getLogger(Param.class);

	public abstract String key();

	public abstract String value();

	public abstract String toString();

}
