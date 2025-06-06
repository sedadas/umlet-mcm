<script setup lang="ts">
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from "@/components/ui/dialog";
import { Card, CardContent } from "@/components/ui/card";
import {Configuration, Version} from "@/types/Configuration.ts";
import {ref} from "vue";
import {Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Button} from "@/components/ui/button";
import {Diff, LoaderCircleIcon} from "lucide-vue-next";
import {compareTwoVersions} from "@/api/configuration.ts";
import { html as diff2html, parse } from 'diff2html';
import 'diff2html/bundles/css/diff2html.min.css';

//props related
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  currentConfiguration: {
    type: Object as () => Configuration,
    required: true
  },
  versionList: {
    type: Array as () => Version[],
    required: true
  }
});
const emit = defineEmits<{
  'update:isOpen': [value: boolean]
}>()

//variables
const firstSelected = ref<string | undefined>(props.currentConfiguration.version.hash)
const secondSelected = ref<string | undefined>(undefined)
const errorMessage = ref<string | undefined>(undefined)
const isLoading = ref(false)
const diffHtml = ref<string>('')

//functions
const closeDialog = () => {
  errorMessage.value = undefined
  diffHtml.value = ''
  firstSelected.value = props.currentConfiguration.version.hash
  secondSelected.value = undefined
  emit('update:isOpen', false)
}

const compareVersions = async () => {
  if (!firstSelected.value || !secondSelected.value || firstSelected.value === secondSelected.value) {
    errorMessage.value = "Please select two different versions to compare"
    return
  }
  isLoading.value = true
  try {
    const diffResult = await compareTwoVersions(props.currentConfiguration.name, firstSelected.value, secondSelected.value)
    // join all the diffs into a single string
    const diffJson = parse(diffResult.map((diff) => diff.content).join('\n'))

    if(diffJson.length === 0) {
      errorMessage.value = "No differences found between the two versions"
    } else {
      errorMessage.value = undefined
      diffHtml.value = diff2html(diffJson, {
        drawFileList: false,
        matching: 'lines',
        outputFormat: 'side-by-side',
      });
    }
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || error.message || "An error occurred while comparing the versions"
  }
  isLoading.value = false
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeDialog">
    <DialogContent class="sm:max-w-[60vw] max-h-[80vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>Version Diff</DialogTitle>
        <DialogDescription>
          See the differences between two versions.
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-4 py-4">
        <Card>
          <CardContent class="p-4 space-y-4">
            <div class="space-y-2">
              <label class="text-sm font-medium">Select two versions to compare</label>
            </div>

            <div class="flex gap-2">
              <Select v-model="firstSelected">
                <SelectTrigger>
                  <SelectValue placeholder="Select a version" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <div v-for="version in versionList.values()" :key="version.hash">
                      <SelectItem :value="version.hash">
                        {{ version.customName ?? version.name }}
                      </SelectItem>
                    </div>
                  </SelectGroup>
                </SelectContent>
              </Select>
              <Select v-model="secondSelected">
                <SelectTrigger>
                  <SelectValue placeholder="Select a version" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <div v-for="version in versionList.values()" :key="version.hash">
                      <SelectItem :value="version.hash">
                        {{ version.customName ?? version.name }}
                      </SelectItem>
                    </div>
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>
            <div class="flex flex-col items-center">
              <Button :disabled="!firstSelected || !secondSelected || (firstSelected === secondSelected) || isLoading" @click="compareVersions">
                <LoaderCircleIcon v-if="isLoading" class="animate-spin"/>
                <Diff v-else class="mr-2" />
                Compare
              </Button>
              <label v-if="errorMessage" class="text-sm font-medium text-red-500">{{errorMessage}}</label>
            </div>
          </CardContent>
        </Card>

        <Card v-if="diffHtml">
          <CardContent class="p-4">
            <div v-html="diffHtml" class="diff-container"></div>
          </CardContent>
        </Card>
      </div>
    </DialogContent>
  </Dialog>
</template>

<style scoped>
.diff-container :deep(.d2h-diff-table) {
  table-layout: fixed;
  width: 100%;
}

.diff-container :deep(tr) {
  width: 100%;
  display: inline-table;
}

.diff-container :deep(.d2h-diff-tbody) {
  display:table;
}
</style>