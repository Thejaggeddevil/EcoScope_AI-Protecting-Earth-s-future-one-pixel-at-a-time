from __future__ import annotations
import os
from datetime import datetime, timedelta
from typing import Union
import ee
import geemap

def _mask_s2_clouds(img: ee.image.Image) -> ee.image.Image:
    qa = img.select("QA60")
    cloud = 1 << 10
    cirrus = 1 << 11
    mask = qa.bitwiseAnd(cloud).eq(0).And(qa.bitwiseAnd(cirrus).eq(0))
    return img.updateMask(mask)

def _parse_date(date_str: str) -> datetime:
    return datetime.strptime(date_str, "%Y-%m-%d")

def download_image(
    lat: Union[str, float],
    lon: Union[str, float],
    date: str,
    out_path: str,
    *,
    days_tolerance: int = 15,
) -> None:
    if not ee.data._initialized:#type: ignore
        ee.Initialize(project="airy-boulevard-457107-s9")

    point = ee.Geometry.Point([float(lon), float(lat)])#type: ignore
    region = point.buffer(500).bounds()

    target = _parse_date(date)
    start_date = (target - timedelta(days=days_tolerance)).strftime("%Y-%m-%d")
    end_date = (target + timedelta(days=days_tolerance)).strftime("%Y-%m-%d")

    collection = (
        ee.ImageCollection("COPERNICUS/S2_SR")#type: ignore
        .filterBounds(point)
        .filterDate(start_date, end_date)
        .sort("CLOUD_COVER")
        .map(_mask_s2_clouds)
    )

    if collection.size().getInfo() == 0:
        raise RuntimeError(
            f"No Sentinel‑2 images found for ({lat}, {lon}) between "
            f"{start_date} and {end_date}"
        )

    image = collection.first()
    if image is None or image.bandNames().size().getInfo() == 0:
        raise RuntimeError("Unable to retrieve a valid Sentinel‑2 image object.")

    rgb_vis = image.select(["B4", "B3", "B2"]).visualize(min=0, max=3000)

    os.makedirs(os.path.dirname(out_path), exist_ok=True)

    geemap.ee_export_image(
        ee_object=rgb_vis,
        filename=out_path,
        scale=10,
        region=region.getInfo(),
        file_per_band=False,
    )
