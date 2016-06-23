package net.smartcosmos.dao.metadata.util;

import com.fasterxml.uuid.Generators;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UuidUtil {

    private static final String URN_PREFIX = "urn";
    private static final String URN_SEPARATOR = ":";

    private static final String UUID_TYPE = "uuid";

    private static final String TENANT_PREFIX = "tenant";
    private static final String USER_PREFIX = "user";
    private static final String THING_PREFIX = "thing";

    public static UUID getUuidFromUrn(String urn) throws IllegalArgumentException {

        String urnScheme="urn:.*:uuid:([A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{12})";

        Pattern p = Pattern.compile(urnScheme, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(urn);
        if (m.find()) {
            return UUID.fromString(m.group(1));
        }

        throw new IllegalArgumentException(String.format("Provided URN '%s' does not match the required URN scheme '%s'", urn, "urn:{prefix}:uuid:{uuid}"));
    }

    public static String getThingUrnFromUuid(UUID uuid) {
        return getPrefixUrnFromUuid(THING_PREFIX, uuid);
    }

    public static String getTenantUrnFromUuid(UUID uuid) {
        return getPrefixUrnFromUuid(TENANT_PREFIX, uuid);
    }

    public static String getUserUrnFromUuid(UUID uuid) {
        return getPrefixUrnFromUuid(USER_PREFIX, uuid);
    }

    static String getPrefixUrnFromUuid(String prefix, UUID uuid) {
        return new StringBuilder(URN_PREFIX)
            .append(URN_SEPARATOR)
            .append(prefix)
            .append(URN_SEPARATOR)
            .append(UUID_TYPE)
            .append(URN_SEPARATOR)
            .append(uuid.toString())
            .toString()
            .toLowerCase();
    }

    public static UUID getNewUuid() {

        String baseUuidString = Generators.timeBasedGenerator().generate().toString();
        String[] parts = baseUuidString.split("-");

        String sortedUuidString = new StringBuilder(36)
            .append(parts[2]).append(parts[1]).append("-")
            .append(parts[0].substring(0, 4)).append("-")
            .append(parts[0].substring(4, 8)).append("-")
            .append(parts[3]).append("-")
            .append(parts[4]).toString();

        return UUID.fromString(sortedUuidString);
    }
}
