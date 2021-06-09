FROM node:12

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
# A wildcard is used to ensure both package.json AND package-lock.json are copied
# where available (npm@5+)
COPY package*.json ./
#COPY htmlviews ./
COPY server.js ./
COPY views ./
COPY public ./
COPY MultiView-DataAPI.js ./

RUN npm install

# If you are building your code for production
# RUN npm ci --only=production

# Bundle app source
COPY . .

EXPOSE 3011
CMD [ "node", "server.js" ]
