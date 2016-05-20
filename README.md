# MaiDonRoad 別擋路
An android app reveals today's road constructions info in Taipei and you can avoid them.

# Dataset
- [台北市今日施工資訊](http://data.taipei/opendata/datalist/datasetMeta/outboundDesc?id=4d29818c-a3ee-425d-b88a-22ac0c24c712&rid=201d8ae8-dffc-4d17-ae1f-e58d8a95b162)

## Dev Env Info
- java@1.8.0.92
- Android Studio@2.1
- Min API: 15
- Target API: 23

## Features
- A google map marks the road constructions location
- Detect your location and pan to it (Warning: if you run on AVD and send no location to it, the default location will set to Tapei Railway Station.)
- A badge on `Toolbar` shows how many constructions are there
- The practice suits SDK 23

## Architect
- Build project using Android Studio
- Fetch open dataset using `AsyncTask`
- Map by Google Map API for Android
- Detect Location via `LocationServices`

## Preview
[preview]: preview.png
