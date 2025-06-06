<script setup lang="ts">
import {Button} from '@/components/ui/button'
import QueryEditor from "@/components/main-content/QueryEditor.vue"
import GraphVisualisation from "@/components/main-content/GraphVisualisation.vue"
import {onMounted, ref, watch} from "vue"
import {FileOutput, HelpCircle, LoaderCircleIcon, Play, RotateCcw, Table2} from 'lucide-vue-next'
import {Model} from "@/types/Model.ts";
import {Node, Relation} from "@/types/Node.ts";
import {exportQueryToCsv, exportQueryToUxf, loadConfigurationDatabase, sendRequest} from "@/api/graphDB.ts";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import QueryResult from "@/components/main-content/QueryResult.vue";
import {parseResponseGraph} from "@/components/main-content/responseGraphVisualization.ts";
import {Configuration} from "@/types/Configuration.ts";
import TableContent from "@/components/main-content/TableContent.vue";
import {alignModels} from "@/api/model.ts";

/**
 * @param {Model} selectedModel, model to display (optional)
 * @param {Configuration} selectedConfiguration, current configuration
 */
const props = defineProps({
  selectedModel: {
    type: Object as () => Model,
    required: false
  },
  selectedConfiguration: {
    type: Object as () => Configuration,
    required: true
  }
});

/**
 * @emits {Node | Relation} update:selectedEntity, selected entity
 * @emits {Record<string, any>[]} update:response, response from the query
 */
const emit = defineEmits<{
  'update:selectedEntity': [entity: Node | Relation],
  'update:response': [response: Record<string, any>[]]
}>()

// variables
const query = ref('')
const queryResponse = ref<Record<string, any>[]>([])
const errorMessage = ref<string | undefined>(undefined)
const queryMessage = ref<string | undefined>(undefined)
const queryGraph = ref<Model | undefined>(undefined);
const queryNum = ref(0)
const activeTab = ref('full')
const isLoadingQuery = ref(false)
const isLoadingUXFCSV = ref(false)
let queryExecutionTimestamp: string | undefined;
const queryGeneratedGraph = ref<string | undefined>(undefined)
const reasonLoadDatabase = ref<string | undefined>(undefined)
const isLoadingNeo4j = ref(false);

// functions
/**
 * Execute multiple queries in a row
 * Uses the sendRequest function from the graphDB API
 * @param queries to execute
 */
const executeMultipleQuery = async (queries: string[]) => {
  let nbOk = 0, totalTime = 0, response
  errorMessage.value = undefined
  queryMessage.value = undefined

  try {
    for (const query of queries.filter(Boolean)) {
      // for all queries, measure the time it takes to execute
      const startTime = performance.now()
      response = await sendRequest(query)
      totalTime += performance.now() - startTime
      nbOk++
      queryNum.value++
    }
    queryExecutionTimestamp = new Date().toLocaleString("de-AT");
  } catch (error: any) {
    // if an error occurs, display the error message and the number of queries executed successfully
    queryMessage.value = nbOk ?
        `${nbOk} ${nbOk === 1 ? "query" : "queries"} executed successfully in ${totalTime} ms. Error on query ${nbOk + 1}`
        : undefined
    errorMessage.value = error.response?.data?.Message || error.message
  }

  // display the last response
  if (response) {
    queryResponse.value = response
    emit('update:response', response)
  }
  if (!errorMessage.value) {
    queryMessage.value = `${nbOk} ${nbOk === 1 ? "query" : "queries"} executed successfully in ${totalTime} ms.`
  }
};

/**
 * Execute the query in the editor
 * Split the query by ';' and execute each query
 */
const executeQuery = async () => {
  if (!query.value?.trim()) return
  isLoadingQuery.value = true
  query.value = query.value.trim().replace("/\n/g", "")
  const formattedQuery = query.value.endsWith(";") ? query.value : `${query.value};`
  await executeMultipleQuery(formattedQuery.split(";").filter(Boolean))
  isLoadingQuery.value = false
};

const uxfCsvExportFull = async (type:string) => {
  isLoadingUXFCSV.value = true
  try {
    if(queryGeneratedGraph.value)
      if(type == "uxf") await exportQueryToUxf(queryGeneratedGraph.value, 'QueryUXF')
      else await exportQueryToCsv(queryGeneratedGraph.value, 'QueryCSV')
  } catch (error: any) {
    // todo add error pop up
  }
  isLoadingUXFCSV.value = false;
}


function combine(selectedModels: Model[]): Model {
  const nodes = selectedModels.flatMap(model => model.nodes)

  return {
    id: "RequestModel",
    description: "",
    mcmAttributes: {},
    originalText: "",
    title: "Merged Model",
    tags: [],
    nodes: nodes,
    zoomLevel: 10
  };
}

// when the response changes, parse it to a graph
watch(() => queryResponse.value, async (newValue) => {
  queryGeneratedGraph.value = undefined
  if (newValue.length > 0) {
    if(props.selectedModel) {
      const nodeModels = await parseResponseGraph(newValue, props.selectedConfiguration.models)
      queryGraph.value = combine(await alignModels(nodeModels))
      queryGeneratedGraph.value = query.value
      if (queryGraph.value.nodes.length > 0 && activeTab.value === 'full') activeTab.value = "requestfull"
    }
  } else {
    queryGraph.value = undefined;
  }
});

watch(() => props.selectedConfiguration.version.hash, async () => {
  await loadNeo4JDatabase()
});

/**
 * Load the Neo4J database
 * Uses the loadConfigurationDatabase function from the graphDB API
 * Uses the sendRequest function from the graphDB API to check if the database is loaded
 */
const loadNeo4JDatabase = async () => {
  isLoadingNeo4j.value = true;
  try {
    await loadConfigurationDatabase(props.selectedConfiguration);
    const checkID = await sendRequest("MATCH (c:Configuration) RETURN c");
    if(checkID.length !== 1 || checkID[0].c.properties.name !== props.selectedConfiguration.name)
      reasonLoadDatabase.value = checkID.length !== 1 ? "No or multiple configuration found" : "Configuration name doesn't match";
    else reasonLoadDatabase.value = undefined;
  } catch(error: any) {
    reasonLoadDatabase.value = error.response?.data?.error || error.message;
  }
  isLoadingNeo4j.value = false;
};

onMounted(() => {
  loadNeo4JDatabase()
});
</script>

<template>
  <div class="flex-1 flex flex-col">
    <div class="p-4 space-y-4">
      <div class="flex items-center ">
        <h2 class="text-xl font-semibold">Neo4J Query</h2>
        <span title="Focus the query field and press Ctrl+Space to use predefined queries (or just start typing)">
          <HelpCircle class="w-4 h-4 ml-2"/>
        </span>
        <div class="ml-auto text-right">
          <div v-if="isLoadingNeo4j">
            <LoaderCircleIcon class="animate-spin"/>
          </div>
          <div v-else>
            <label v-if="reasonLoadDatabase" class="text-sm content-center text-red-500">Neo4J doesn't match current configuration:{{reasonLoadDatabase}}</label>
            <label v-else class="text-sm content-center text-green-500">Neo4J data loaded</label>
            <Button v-if="reasonLoadDatabase" variant="outline" size="icon" class="ml-2" @click="loadNeo4JDatabase">
              <RotateCcw/>
            </Button>
          </div>
        </div>
      </div>
      <QueryEditor v-model:query="query" />
      <div class="flex gap-2">
        <Button @click="executeQuery" class="flex items-center" :disabled="isLoadingQuery">
          <Play v-if="!isLoadingQuery" class="mr-2 h-4 w-4" />
          <LoaderCircleIcon v-else class="animate-spin"/>
          Execute Query
        </Button>
        <label v-if="queryMessage" class="text-sm text-green-500 content-center">{{ "["+queryNum+"] " + queryMessage }}</label>
        <label v-if="errorMessage" class="text-sm text-red-500 content-center">{{ "["+queryNum+"] " + errorMessage }}</label>
      </div>
    </div>
    <Tabs default-value="full" v-model:model-value="activeTab" class="h-full w-full overflow-hidden p-2">
      <TabsList>
        <TabsTrigger value="full">Model Graph</TabsTrigger>
        <TabsTrigger value="request">Response filtered model</TabsTrigger>
        <TabsTrigger value="requestfull">Response database</TabsTrigger>
        <TabsTrigger value="table">Response as table</TabsTrigger>
        <TabsTrigger value="json">Response as JSON</TabsTrigger>
      </TabsList>

      <TabsContent value="full" class="h-[95%]">
        <div v-if="selectedModel" class="h-full w-full">
            <GraphVisualisation
                :model-to-display="selectedModel"
                @update:selectedEntity="emit('update:selectedEntity', $event)"/>
        </div>
        <div v-else class="h-full w-full">
          <div class="flex-1 flex justify-center h-full">
            <p class="text-muted-foreground self-center">No model selected</p>
          </div>
        </div>
      </TabsContent>
      <TabsContent value="request" class="h-[95%] relative">
        <div v-if="queryGraph && selectedModel" class="h-full w-full">
          <GraphVisualisation
              :model-to-display="{
                      ...queryGraph,
                      nodes: queryGraph.nodes.filter(n =>
                          selectedModel!.nodes.map(m => m.id).includes(n.id)
                      )
                  }"
              @update:selectedEntity="emit('update:selectedEntity', $event)"/>
        </div>
        <div v-else class="h-full w-full">
          <div class="flex-1 flex justify-center h-full">
            <p class="text-muted-foreground self-center">No query has been executed yet</p>
          </div>
        </div>
      </TabsContent>
      <TabsContent value="requestfull" class="h-[95%] relative">
        <div v-if="queryGraph" class="h-full w-full">
          <GraphVisualisation
              :model-to-display="queryGraph"
              @update:selectedEntity="emit('update:selectedEntity', $event)"/>
          <div class="absolute top-0 right-0 m-2">
            <Button
                class="p-2 m-1"
                size="icon"
                v-tooltip="'Export query result to UXF'"
                @click="uxfCsvExportFull('uxf')"
                :disabled="isLoadingUXFCSV">
              <LoaderCircleIcon v-if="isLoadingUXFCSV" class="animate-spin"/>
              <FileOutput v-else/>
            </Button>
            <Button
                class="p-2 m-1"
                size="icon"
                v-tooltip="'Export query result to CSV'"
                @click="uxfCsvExportFull('csv')"
                :disabled="isLoadingUXFCSV">
              <LoaderCircleIcon v-if="isLoadingUXFCSV" class="animate-spin"/>
              <Table2 v-else/>
            </Button>
          </div>
        </div>
        <div v-else class="h-full w-full">
          <div class="flex-1 flex justify-center h-full">
            <p class="text-muted-foreground self-center">No query has been executed yet</p>
          </div>
        </div>
      </TabsContent>
      <TabsContent value="table" class="h-[95%] overflow-scroll">
        <TableContent :queryResponse="queryResponse" :queryNum="queryNum" :queryTimestamp="queryExecutionTimestamp"/>
      </TabsContent>
      <TabsContent value="json" class="h-[95%] overflow-scroll">
        <QueryResult :queryResponse="queryResponse"/>
      </TabsContent>
    </Tabs>
  </div>
</template>