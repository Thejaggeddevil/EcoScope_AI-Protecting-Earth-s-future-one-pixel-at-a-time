import numpy as np

def get_change_area(mask):
    total = mask.size
    changed = np.count_nonzero(mask)
    return round((changed / total) * 100, 2)

def get_reason(mask):
    values, counts = np.unique(mask, return_counts=True)
    if len(values) <= 1:
        return "No change"
    main_class = values[1:][np.argmax(counts[1:])]
    reason_map = {
        1: "Glacial Lake Expansion",
        2: "New Road Construction",
        3: "Drainage Network Change",
        4: "Urban Expansion"
    }
    return reason_map.get(main_class, "Unknown")

def get_impact(reason):
    impact_map = {
        "Glacial Lake Expansion": "Risk of glacial flooding",
        "New Road Construction": "Land use disturbance",
        "Drainage Network Change": "Flooding or waterlogging risk",
        "Urban Expansion": "Urban heat or congestion"
    }
    return impact_map.get(reason, "No major impact")
