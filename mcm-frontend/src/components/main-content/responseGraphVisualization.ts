import { Model } from "@/types/Model.ts";
import { Node } from "@/types/Node.ts";

/**
 * Parses the nodes from the query response and reconstruct a model.
 * @param response The response from the query
 * @param modelList The list of models to which the nodes should be added
 * @return The reconstructed model with the nodes and relations from the response
 */
export async function parseResponseGraph(response: Record<string, any>[], modelList: Model[]): Promise<Model[]> {
    // Detect nodes in the response
    const detectedNodes = detectNodes(response);
    const nodesModelList: Node[] = modelList.flatMap((model) => model.nodes);

    const nodes: Node[] = [];
    detectedNodes.forEach((rawNode: any) => {
        if (nodes.some((n) => n.id === rawNode.properties.generatedID)) return;
        if (Object.keys(rawNode.properties).length > 0 && rawNode.properties.name) {
            const n = nodesModelList.find((node) => node.id === rawNode.properties.generatedID);
            if(n) {
                // update the properties of the node with the ones from the response
                const deepNode = JSON.parse(JSON.stringify(n));
                deepNode.mcmAttributes = extractAttributes(rawNode.properties, "properties")
                deepNode.umletAttributes = extractAttributes(rawNode.properties, "umletProperties")
                nodes.push(deepNode)
            }
        }
    });

    const retModel: Record<string, Model> = {};
    nodes.forEach(node => {
        const modelId = node.mcmModelId;
        const model = modelList.find((m) => m.id === modelId);
        if (!retModel[modelId] && model) {
            retModel[modelId] = { ...model, id: modelId, nodes: [] };
        }
        retModel[modelId].nodes.push(node);
    });

    return Object.values(retModel);
}

/**
 * Detects the nodes in the response and returns them as an array.
 * @param response The response from the query
 * @return An array of raw node objects
 */
function detectNodes(response: Record<string, any>[]): any[] {
    const nodes: any[] = [];
    for (let item of response) {
        for (let key in item) {
            const obj = item[key];
            if (typeof obj === "object") {
                if (obj.nodes) {
                    // some queries return nodes in an array
                    obj.nodes.forEach((n: Object) => nodes.push(n));
                } else if (obj.labels && obj.labels.includes("Node")) {
                    // some queries return nodes as objects
                    nodes.push(obj);
                }
            }
        }
    }
    return nodes;
}

/**
 * Extracts attributes from the object that have a specific prefix.
 * @param obj The object from which the attributes should be extracted
 * @param prefix The prefix that the attributes should have
 * @return An object with the extracted attributes
 */
function extractAttributes(obj: Record<string, any>, prefix: string): Record<string, any> {
    // Filter the object for keys that start with the given prefix and extract the values
    return Object.entries(obj)
        .filter(([key]) => key.startsWith(prefix))
        .reduce((result, [key, value]) => {
            const keyWithoutPrefix = key.split('.')[1];
            result[keyWithoutPrefix] = value;
            return result;
        }, {} as Record<string, any>);
}