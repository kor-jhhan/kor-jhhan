# wordpress On kubernetes

## 목차
- [[Step1] NFS 구축](#contents1)
- [[Step2] NameSpace 생성](#contents2)
- [[Step3] PVC 생성](#contents3)
- [[Step4] Deployment 생성](#contents4)
- [[Step5] Service 생성](#contents5)
- [[Step6] wordpress 접근](#contents6)


## 사용 이미지
 - mysql:5.7
 - wordpress:4.8-apache

<div id="contents1"></div>

## [Step1] NFS 구축 및 NFS StorageClass만들기
참고 : https://momobob.tistory.com/50

- StorageClass만들기
1. GitHub 받기 
    ```bash
    # 작업할 위치에서 아래 명령어로 예제를 가지고 온다.
    $ git clone https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner
    $ ls 
      nfs-subdir-external-provisioner
    ```
2. Namespace 생성
    ```bash
    $ kubectl create namespace nfs-provisioner
    ```
3. 예제 yaml을 변경하여 롤부여
    ```bash
    ## RBAC(Role-Based Access Control) 롤 부여
    $ cd ./nfs-subdir-external-provisioner/deploy/
    $ sed -i "s/namespace:.*/namespace: nfs-provisioner/g" ./rbac.yaml ./deployment.yaml
    ## 배포 
    $ kubectl apply -f ./rbac.yaml
    ```

4. nfs-provisioner app 배포 (PV를 대신 만들어 주는 역할)
    ```yaml
    ## deployment.yaml 생성
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: nfs-client-provisioner
      labels:
        app: nfs-client-provisioner
      namespace: nfs-provisioner
    spec:
      replicas: 1
      strategy:
        type: Recreate
      selector:
        matchLabels:
          app: nfs-client-provisioner
      template:
        metadata:
          labels:
            app: nfs-client-provisioner
        spec:
          serviceAccountName: nfs-client-provisioner
          containers:
            - name: nfs-client-provisioner
              image: k8s.gcr.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2
              volumeMounts:
                - name: nfs-client-root
                  mountPath: /root/nfs #변경
              env:
                - name: PROVISIONER_NAME
                  value: k8s-sigs.io/nfs-subdir-external-provisioner
                - name: NFS_SERVER
                  value: 192.168.56.101 #변경
                - name: NFS_PATH
                  value: /root/nfs #변경
          volumes:
            - name: nfs-client-root
              nfs:
                server: 192.168.56.101 #변경
                path: /root/nfs #변경
    ```

    ```bash
    $ kubectl create ./deployment.yaml 
    $ kubectl get all -n nfs-provisioner
    ```

6. storageclass 생성
    ```yaml
    ## nfs_storageclass.yaml 생성
    apiVersion: storage.k8s.io/v1
    kind: StorageClass
    metadata:
      annotations:
          storageclass.kubernetes.io/is-default-class: "true"
      name: nfs-client
    parameters:
      onDelete: retain  ## delete일 경우 디렉터리 삭제. retain일 경우 디렉토리 유지. 
      pathPattern: ${.PVC.namespace}/${.PVC.name}
    provisioner: k8s-sigs.io/nfs-subdir-external-provisioner
    ```
    ```bash
    $ kubectl create -f ./nfs_storageclass.yaml
    ```

<div id="contents2"></div>

## [Step2] NameSpace 생성
  ```
  $ kubectl create namespace wordpress
  ```

<div id="contents3"></div>

## [Step3] PVC 생성
- mysql 사용 볼륨
  ```yaml
  apiVersion: v1
  kind: PersistentVolumeClaim
  metadata:
    name: mysql-pvc
    namespace: wordpress
    labels:
      app: wordpress
  spec:
    accessModes:
      - ReadWriteOnce
    resources:
      requests:
        storage: 3Gi
  ```
- wordpress 사용 볼륨
  ```yaml
  apiVersion: v1
  kind: PersistentVolumeClaim
  metadata:
    name: wp-pvc
    namespace: wordpress
    labels:
      app: wordpress
  spec:
    accessModes:
      - ReadWriteOnce
    resources:
      requests:
        storage: 1Gi
  ```

<div id="contents4"></div>

## [Step4] Deployment 생성
- mysql 서비스 배포
  ```yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: wordpress-mysql
    namespace: wordpress
    labels:
      app: wordpress
  spec:
    selector:
      matchLabels:
        app: wordpress
        tier: mysql
    strategy:
      type: Recreate
    template:
      metadata:
        labels:
          app: wordpress
          tier: mysql
      spec:
        containers:
        - image: mysql:5.7
          name: mysql
          env:
          - name: MYSQL_ROOT_PASSWORD
            value: qwer1234
          ports:
          - containerPort: 3306
            name: mysql
          volumeMounts:
          - name: mysql-persistent-storage
            mountPath: /var/lib/mysql
        volumes:
        - name: mysql-persistent-storage
          persistentVolumeClaim:
            claimName: mysql-pvc
  ```
- wordpress 서비스 배포
  ```yaml
  apiVersion: apps/v1
  kind: ReplicaSet
  metadata:
    name: wordpress
    namespace: wordpress
    labels:
      app: wordpress
  spec:
    replicas: 2
    selector:
      matchLabels:
        app: wordpress
        tier: frontend
    template:
      metadata:
        labels:
          app: wordpress
          tier: frontend
      spec:
        containers:
        - image: wordpress:4.8-apache
          name: wordpress
          env:
          - name: WORDPRESS_DB_HOST
            value: wordpress-mysql
          - name: WORDPRESS_DB_PASSWORD
            value: qwer1234
          ports:
          - containerPort: 80
            name: wordpress
          volumeMounts:
          - name: wordpress-persistent-storage
            mountPath: /var/www/html
        volumes:
        - name: wordpress-persistent-storage
          persistentVolumeClaim:
            claimName: wp-pvc
  ```

<div id="contents5"></div>

## [Step5] Service 생성
- mysql 서비스
  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: wordpress-mysql
    namespace: wordpress
    labels:
      app: wordpress
  spec:
    type: ClusterIP
    ports:
      - protocol: TCP
        port: 3306
    selector:
      app: wordpress
      tier: mysql
  ```
- wordpress 서비스
  ```yaml
  apiVersion: v1
  kind: Service
  metadata:
    name: wordpress
    namespace: wordpress
    labels:
      app: wordpress
  spec:
    ports:
      - protocol: TCP
        port: 80
    selector:
      app: wordpress
      tier: frontend
    type: NodePort
  ```

<div id="contents6"></div>

## [Step6] wordpress 접근
https://localhost:{NodePort}/