= Notes on the Pipeline Setup

* The pipeline uses custom [`git-clone`](tasks/git-clone.yaml) and [`kubernetes-actions`](tasks/kubernetes-actions.yaml)
  tasks because the default ClusterTasks on the cluster don't work ("Error: container has runAsNonRoot and image will
  run as root (pod: "test-taskrun-4l6zv-pod_jboss-set-psi(4c8924d9-ff5b-4593-9453-986d0d000567)", container: place-scripts)").
* The pipeline uses custom [`maven-quarkus`](tasks/maven-quarkus.yaml) task which sets higher memory limits, need to 
  compile native quarkus binary.

== Pipeline Installation

```shell
oc create -f tasks/git-clone.yaml
oc create -f tasks/maven-quarkus.yaml
oc create -f tasks/kubernetes-actions.yaml
oc create -f pipeline-native.yaml
oc create -f cronjob.yaml
```

== Run Pipeline Manually

```shell
tkn pipeline start component-upgrades-logger-native-pipeline -n jboss-set-psi \
  --serviceaccount=build-bot \
  -w name=shared-workspace,claimName=build-volume,subPath=component-upgrade-logger \
  -w name=maven-settings,emptyDir= \
  -w name=kubeconfig-workspace,emptyDir= \
  --use-param-defaults
```

== Issues

* Why don't ClusterTasks work?
