package web.param;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class GlobalParams {

	private final List<Param> GPS = new ArrayList<>();
	private static final String SPLIT = "&";

	private GlobalParams(){}

	public static final GlobalParams of( HttpServletRequest request){
		GlobalParams globalParams = new GlobalParams();
        globalParams.GPS.add( UsidParam.of(request) );
        globalParams.GPS.add( SgidParam.of(request) );
		return globalParams;
	}

	@Override
	public final String toString(){
		StringBuilder builder = new StringBuilder(256);
		for( Param p : GPS ){
			builder.append(p.toString()).append(SPLIT);
		}
		if( !GPS.isEmpty() )
			builder.delete(builder.length() - SPLIT.length(),builder.length());
		return builder.toString();
	}

    public List<Param> getGPS() {
        return GPS;
    }
}
