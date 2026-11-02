package us.z1x.framejob;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.ConfigEntrypoint;
import org.polyfrost.oneconfig.api.config.v1.Tree;
import org.polyfrost.oneconfig.api.config.v1.collect.impl.OneConfigCollector;

public class FramejobConfig extends Config {
    private FramejobConfig() {
        super("framejob.json", "Framejob", Category.VISUALS);
    }

    @Override
    protected Tree makeTree() {
        Tree tree = Tree.tree(id);
        new OneConfigCollector().handle(tree, new Framejob(), 0);
        return tree;
    }

    public static void register(ConfigEntrypoint.Context context) {
        new FramejobConfig();
    }
}
