# tutorio backend
## compilation: 
```bash
mvn clean install
```
## start: 
```bash
java -jar tutorio-backend.jar
```
## deploy on docker: 
```bash
docker compose up --build
```
## endpoints documentation: http://54.37.138.92:8085/swagger-ui/index.html
## auto-deploy with git checking:
```bash
chmod +x deploy.sh
./deploy.sh 
```
