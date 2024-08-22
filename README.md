# Infinite Billboard
The Infinite Billboard is an experimental website that tries to reimagine online interaction. Users are free to place images anywhere on the board, which includes other people's images, with the creative limitation of working with actual pixels of a "single" image. The position on the map can be easily bookmarked and shared by the URL which enables complex interaction with secret locations.

For frontend see: https://github.com/Jobieskii/InfiniteBillboardFrontend
## Backend
This is the backend server. It stores and generates images in a file structure that can be used to serve as maps by e.g. leaflet.js. All image processing is done in a concurrent manner (configurable). The images themselves need to be served by a seperate file server. This server also includes crude authorization and validation using a custom enpoint from my other website.

## API
The server exposes only a couple endpoints
- PATCH /tiles/{x}/{y} – which accepts an image file and optional scale to be applied. Optionally if the server is configured as such it will check for authorization. Each request is recorded in the database.
- GET /session – validates users identity and returns it, if it's correct.
- WS /stomp – For connecting to the STOMP server

## Architecture
The service is deployed using docker compose on a server behind a proxy. This proxy also acts as the file server required to serve files and serving frontend. Because of this I don't need to set additional cross origin configurations that would be typical for an api backend.
### Database model
![image](https://github.com/user-attachments/assets/5ebfd836-e139-4d9a-bfac-208fe52e231f)
