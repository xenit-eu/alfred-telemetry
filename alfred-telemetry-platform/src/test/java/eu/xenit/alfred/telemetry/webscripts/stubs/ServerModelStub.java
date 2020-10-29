package eu.xenit.alfred.telemetry.webscripts.stubs;

import java.util.UUID;
import org.springframework.extensions.webscripts.ServerModel;

public class ServerModelStub implements ServerModel {

    private static final String EDITION_ENTERPRISE = "Enterprise";
    private static final String EDITION_COMMUNITY = "Community";

    private final String id;
    private final int versionMajor;
    private final int versionMinor;
    private final int versionRevision;
    private final String label;
    private final String build;
    private final int schema;
    private final String edition;

    private ServerModelStub(String id, int versionMajor, int versionMinor, int versionRevision,
            String label, String build, int schema, String edition) {
        this.id = id;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.versionRevision = versionRevision;
        this.label = label;
        this.build = build;
        this.schema = schema;
        this.edition = edition;
    }

    public static ServerModel enterprise_52() {
        return new ServerModelStub(UUID.randomUUID().toString(), 5, 2, 7, ".3", "rd418b4e5-b31", 10095, EDITION_ENTERPRISE);
    }

    @Override
    public String getContainerName() {
        return "Repository";
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return "Main Repository";
    }

    @Override
    public String getVersionMajor() {
        return String.valueOf(this.versionMajor);
    }

    @Override
    public String getVersionMinor() {
        return String.valueOf(this.versionMinor);
    }

    @Override
    public String getVersionRevision() {
        return String.valueOf(this.versionRevision);
    }

    @Override
    public String getVersionLabel() {
        return this.label;
    }

    @Override
    public String getVersionBuild() {
        return this.build;
    }

    @Override
    public String getVersion() {
        StringBuilder version = new StringBuilder(getVersionMajor());
        version.append(".");
        version.append(getVersionMinor());
        version.append(".");
        version.append(getVersionRevision());

        String label = getVersionLabel();
        String build = getVersionBuild();

        boolean hasLabel = label != null && label.length() > 0;
        boolean hasBuild = build != null && build.length() > 0;

        // add opening bracket if either a label or build number is present
        if (hasLabel || hasBuild)
        {
            version.append(" (");
        }

        // add label if present
        if (hasLabel)
        {
            version.append(label);
        }

        // add build number is present
        if (hasBuild)
        {
            // if there is also a label we need a separating space
            if (hasLabel)
            {
                version.append(" ");
            }

            version.append(build);
        }

        // add closing bracket if either a label or build number is present
        if (hasLabel || hasBuild)
        {
            version.append(")");
        }

        return version.toString();
    }

    @Override
    public String getEdition() {
        return this.edition;
    }

    @Override
    public int getSchema() {
        return this.schema;
    }
}
