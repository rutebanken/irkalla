# Contains main description of bulk of terraform?
terraform {
  required_version = ">= 0.13.2"
}

provider "google" {
  version = ">= 4.26"
}
provider "kubernetes" {
  version = ">= 2.13.1"
}



# TODO: Create storage bucket
# Create bucket
#resource "google_storage_bucket" "storage_bucket" {
#  name               = "${var.bucket_instance_prefix}-${var.bucket_instance_suffix}"
#  force_destroy      = var.force_destroy
#  location           = var.location
#  project            = var.storage_project
#  storage_class      = var.storage_class
#  labels             = var.labels
#  uniform_bucket_level_access = true
#  versioning {
#    enabled = var.versioning
#  }
#  logging {
#    log_bucket        = var.log_bucket
#    log_object_prefix = "${var.bucket_instance_prefix}-${var.bucket_instance_suffix}"
#  }
#}

# Create folder in a bucket
#resource "google_storage_bucket_object" "content_folder" {
#  name          = "StopPlace/"
#  content       = "Not really a directory, but it's empty."
#  bucket        = google_storage_bucket.storage_bucket.name
#}

#Create pubsub topics and subscriptions
# pubsub topics ChouetteStopPlaceSyncQueue, ChouetteStopPlaceDeleteQueue

# Create pubsub topic
resource "google_pubsub_topic" "stopplace-sync-queue" {
  name   = "ChouetteStopPlaceSyncQueue"
  project = var.pubsub_project
  labels = var.labels
}

# Create pubsub subscription
resource "google_pubsub_subscription" "stopplace-sync-queue-subscription" {
  name  = "ChouetteStopPlaceSyncQueue"
  topic = google_pubsub_topic.stopplace-sync-queue.name
  project = var.pubsub_project
  labels = var.labels
}

# Create pubsub topic
resource "google_pubsub_topic" "stopplace-delete-queue" {
  name   = "ChouetteStopPlaceDeleteQueue"
  project = var.pubsub_project
  labels = var.labels
}

# Create pubsub subscription
resource "google_pubsub_subscription" "stopplace-delete-queue-subscription" {
  name  = "ChouetteStopPlaceDeleteQueue"
  topic = google_pubsub_topic.stopplace-delete-queue.name
  project = var.pubsub_project
  labels = var.labels
}

# pubsub topics KafkaStopPlaceDeleteQueue, KafkaStopPlaceSyncQueue

# Create pubsub topic
resource "google_pubsub_topic" "kafka-stopplace-delete-queue" {
  name   = "KafkaStopPlaceDeleteQueue"
  project = var.pubsub_project
  labels = var.labels
}

# pubsub topics  KafkaStopPlaceChangeLog
resource "google_pubsub_topic" "kafka-stopplace-changelog-topic" {
  name   = "KafkaStopPlaceChangelog"
  project = var.pubsub_project
  labels = var.labels
}

# Create pubsub subscription
resource "google_pubsub_subscription" "kafka-stopplace-changelog-subscription" {
  name  = "KafkaStopPlaceChangelog"
  topic = google_pubsub_topic.kafka-stopplace-changelog-topic.name
  project = var.pubsub_project
  labels = var.labels
}

# TODO: Create pubsub subscription tiamat stop.place change log

#resource "google_pubsub_subscription" "tiamat-changelog-subscription" {
#  name ="ror.tiamat.changelog"
#  topic = google_pubsub_topic.tiamat-changelog-topic.name
#}

# Add irkalla secrets

resource "kubernetes_secret" "ror-irkalla-client-secrets" {
  metadata {
    name      = "${var.labels.team}-${var.labels.app}-secrets"
    namespace = var.kube_namespace
  }

  data = {
    "KAFKAUSERNAME"    = var.ror-irkalla-kafka-username
    "KAFKAPASSWORD"    = var.ror-irkalla-kafka-password
  }
}

