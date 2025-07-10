import ee
import geemap

def download_image(lat, lon, date, out_path):
    try:
        ee.Initialize()
    except Exception:
        ee.Initialize()

  
    point = ee.Geometry.Point([float(lon), float(lat)])# type: ignore
    region = point.buffer(500).bounds() 
    collection = (
        ee.ImageCollection('COPERNICUS/S2')# type: ignore
        .filterBounds(point)
        .filterDate(date, date)
        .sort('CLOUDY_PIXEL_PERCENTAGE')
    )
    image = collection.first()

    if image is None:
        raise Exception("No Sentinel-2 image found for this date/location.")
    rgb = image.select(['B4', 'B3', 'B2']).visualize(min=0, max=3000)
    geemap.ee_export_image(
        rgb,
        filename=out_path,
        scale=10, 
        region=region.getInfo(),
        file_per_band=False
    )

