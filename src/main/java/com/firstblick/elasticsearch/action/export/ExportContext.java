package com.firstblick.elasticsearch.action.export;

import com.firstblick.elasticsearch.action.export.parser.ExportOutputFormat;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.service.IndexService;
import org.elasticsearch.index.shard.service.IndexShard;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.SearchShardTarget;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.search.internal.ShardSearchRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for export specific informations.
 */
public class ExportContext extends SearchContext {

    private static final String VAR_SHARD = "${shard}";
    private static final String VAR_INDEX = "${index}";
    private static final String VAR_CLUSTER = "${cluster}";

    private List<String> outputCmdArray;
    private String outputCmd;
    private String outputFile;
    private boolean forceOverride = false;
    private ExportOutputFormat outputFormat = new ExportOutputFormat();

    public ExportContext(long id, ShardSearchRequest request, SearchShardTarget shardTarget, Engine.Searcher engineSearcher, IndexService indexService, IndexShard indexShard, ScriptService scriptService) {
        super(id, request, shardTarget, engineSearcher, indexService, indexShard, scriptService);
    }

    public List<String> outputCmdArray() {
        return outputCmdArray;
    }

    public void outputCmdArray(List<String> outputCmdArray) {
        this.outputCmdArray = applyVars(outputCmdArray);
    }

    public String outputCmd() {
        return outputCmd;
    }

    public void outputCmd(String outputCmd) {
        this.outputCmd = applyVars(outputCmd);
    }

    public String outputFile() {
        return outputFile;
    }

    public void outputFile(String outputFile) {
        this.outputFile = applyVars(outputFile);
    }

    public boolean forceOverride() {
        return forceOverride;
    }

    public void forceOverride(boolean forceOverride) {
        this.forceOverride = forceOverride;
    }

    public ExportOutputFormat outputFormat() {
        return outputFormat;
    }

    public void outputFormat(ExportOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Replaces variable placeholder with actual value in all elements of templateArray
     *
     * @param templateArray
     * @return
     */
    private List<String> applyVars(List<String> templateArray) {
        List<String> ret = new ArrayList<String>();
        for (String part : templateArray) {
            ret.add(applyVars(part));
        }
        return ret;
    }

    /**
     * Replaces variable placeholder with actual value
     *
     * @param template
     * @return
     */
    private String applyVars(String template) {
        template = template.replace(VAR_SHARD, String.valueOf(indexShard().shardId().getId()));
        template = template.replace(VAR_INDEX, indexShard().shardId().getIndex());
        template = template.replace(VAR_CLUSTER, clusterName());
        return template;
    }

    /**
     * Method to retrieve name of cluster
     *
     * @return name of cluster
     */
    private String clusterName() {
        return ClusterName.clusterNameFromSettings(this.indexShard().indexSettings()).value();
    }

    public Output createOutput() {
        if (outputFile()!=null){
            return new OutputFile(outputFile(), forceOverride());
        } else {
            if (outputCmd()!=null){
                return new OutputCommand(outputCmd());
            } else {
                return new OutputCommand(outputCmdArray());
            }
        }
    }
}
