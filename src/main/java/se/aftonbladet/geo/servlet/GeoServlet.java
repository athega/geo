package se.aftonbladet.geo.servlet;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Originally created by @author chrliz at 2013-04-11 13:13
 */
public class GeoServlet extends HttpServlet {
	private static final Logger LOG = Logger.getLogger(GeoServlet.class);
	private static final String PIXEL_B64  = "R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==";
	private static final byte[] PIXEL_BYTES = Base64.decodeBase64(PIXEL_B64.getBytes());

	private static LookupService lookup;
	static {
		try {
			lookup = new LookupService(GeoServlet.class.getClassLoader().getResource("GeoLiteCity.dat").getPath(), LookupService.GEOIP_MEMORY_CACHE);
		} catch (IOException e) {
			LOG.error("Failed to initiate lookup service:", e);
		}
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final int status = checkReferrer(request);
		if (status != 200) {
			response.setStatus(status);
		} else {
			String ip = request.getHeader("X-Forwarded-For");
			if (ip == null) {
				ip = request.getRemoteAddr();
			}

			if (isNorrlandByIp(ip)) {
				response.setContentType("image/gif");
				response.getOutputStream().write(PIXEL_BYTES);
			} else {
				response.setStatus(204); // No content
			}
		}
	}

	private int checkReferrer(final HttpServletRequest request) {
		final String referrer = request.getHeader("referer");
		try {
			if (referrer == null || !new URL(referrer).getHost().endsWith("aftonbladet.se")) {
				if (LOG.isDebugEnabled()) { LOG.debug("403: Referrer not ok: " + referrer); }
				return 403; // Forbidden
			}
		} catch (MalformedURLException e) {
			if (LOG.isDebugEnabled()) { LOG.debug("400: Referrer malformed: " + referrer); }
			return 400; // Bad request
		}
		return 200; // OK
	}

	private boolean isNorrlandByIp(final String ip) {
		boolean isNorrland = false;
		if (lookup != null) {
			final Location location = lookup.getLocation(ip);
			if (location != null && "SE".equals(location.countryCode) && location.region != null) {
				final String region = location.region;
				if (
						"3".equals(region) ||  // Gävleborg
						"7".equals(region) ||  // Jämtland
						"24".equals(region) || // Västernorrland
						"23".equals(region) || // Västerbotten
						"14".equals(region)    // Norrbotten
						) {
					isNorrland = true;
				}
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug(ip + ":" + (location != null ? " location " + location.countryCode + "-" + location.region + ":" + isNorrland : "null"));
			}
		}
		return isNorrland;
	}
}
